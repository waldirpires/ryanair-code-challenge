package com.ryanair.flights.connecting.Service;

import com.ryanair.flights.connecting.api.FlightLeg;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.domain.FlightRoute;
import com.ryanair.flights.connecting.domain.FlightScheduleResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FlightsRouteFinderService {
    private final WebClient webClient;

    public FlightsRouteFinderService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://services-api.ryanair.com").build();
    }

    public List<FlightRoute> getFlightRoutes(String operator) {
        return webClient.get()
                .uri("/views/locate/3/routes")
                .retrieve()
                .bodyToFlux(FlightRoute.class)
                .filter(route -> route.connectingAirport() == null && operator.equals(route.operator()))
                .collectList()
                .block();
    }

    List<FlightLeg> getFlightsForRoute(final FlightRequest request, final String departure, final String arrival) {
        int year = request.departureDateTime().getYear();
        int month = request.departureDateTime().getMonthValue();

        FlightScheduleResult result = webClient.get()
                .uri("/timtbl/3/schedules/{departure}/{arrival}/years/{year}/months/{month}",
                        departure, arrival, year, month)
                .retrieve()
                .bodyToMono(FlightScheduleResult.class)
                .block();

        if (result == null || result.flightDays() == null) {
            return Collections.emptyList();
        }

        return buildFlightLegs(request, departure, arrival, result, month);
    }

    public List<FlightLeg> getFlightsForRoute(final FlightRequest request, final FlightRoute route) {
        return getFlightsForRoute(request, route.airportFrom(), route.airportTo());
    }

    List<FlightLeg> buildFlightLegs(
            final FlightRequest request,
            final String departure,
            final String arrival,
            final FlightScheduleResult result,
            final int month) {
        List<FlightLeg> legs = new ArrayList<>();

        if (result.flightDays().isEmpty()) {
            return legs;
        }

        // for each day and flight
        result.flightDays().forEach(day -> {
            List<FlightScheduleResult.Flight> flights = day.flights();
            if (flights == null || flights.isEmpty()) {
                return;
            }

            int year = request.departureDateTime().getYear();
            flights.forEach(flight -> {
                LocalDateTime departureDateTime = LocalDate.of(
                                year, month, day.day())
                        .atTime(LocalTime.parse(flight.departureTime()));
                LocalDateTime arrivalDateTime = LocalDate.of(year, month, day.day())
                        .atTime(LocalTime.parse(flight.arrivalTime()));

                // if departure is after request and arrival is before request, we add the flight leg
                if (!departureDateTime.isBefore(request.departureDateTime()) &&
                        !arrivalDateTime.isAfter(request.arrivalDateTime())) {
                    legs.add(new FlightLeg(departure, arrival, departureDateTime, arrivalDateTime));
                }
            });
        });

        return legs;
    }
}
