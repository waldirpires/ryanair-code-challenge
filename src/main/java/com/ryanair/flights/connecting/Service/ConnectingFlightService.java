package com.ryanair.flights.connecting.Service;

import com.ryanair.flights.connecting.api.FlightLeg;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.api.FlightResponse;
import com.ryanair.flights.connecting.domain.FlightRoute;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Service
public class ConnectingFlightService {

    public static final int MAX_DELTA_HOURS = 2;
    private final FlightsRouteFinderService routeFinderService;

    public ConnectingFlightService(FlightsRouteFinderService flightsRouteFinderService) {
        this.routeFinderService = flightsRouteFinderService;
    }

    public List<FlightResponse> findFlights(final FlightRequest request) {
        List<FlightResponse> flightResponses = new ArrayList<>();

        // Step 1: Fetch routes available for flight.
        List<FlightRoute> routes = routeFinderService.getFlightRoutes("RYANAIR");

        // Step 2: Filter for direct flights (0 stops) and add to response
        flightResponses.addAll(findDirectFlights(request, routes));

        // Step 3: Check for interconnected flights (1 stop) and add to response
        flightResponses.addAll(findInterconnectedFlights(request, routes, 1));

        return flightResponses;
    }

    List<FlightResponse> findDirectFlights(final FlightRequest request, List<FlightRoute> routes) {
        List<FlightResponse> directFlights = new ArrayList<>();
        routes.stream()
                .filter(route -> route.equalsRequest(request.departure(), request.arrival()))
                .forEach(route -> {
                    List<FlightLeg> legs = routeFinderService.getFlightsForRoute(request, route);
                    legs.forEach(leg -> directFlights.add(new FlightResponse(0, List.of(leg))));
                });
        return directFlights;
    }

    List<FlightResponse> findInterconnectedFlights(final FlightRequest request, List<FlightRoute> routes, final int numStops) {
        List<FlightResponse> interconnectedFlights = new ArrayList<>();

        List<FlightRoute> flightRoutesFromOrigin = ofNullable(routes).orElse(emptyList()).stream()
                .filter(route -> route.airportFrom().equals(request.departure())).toList();

        flightRoutesFromOrigin
                .forEach(originRoute -> Optional.ofNullable(routes).orElse(emptyList()).stream()
                        // route origin equals origin route destination and route destination equals request destination
                        .filter(route2 -> route2.airportFrom().equals(originRoute.airportTo()) && route2.airportTo().equals(request.arrival()))
                        .forEach(route2 -> {
                            interconnectedFlights.addAll(getInterconnectedFlights(request, numStops, originRoute, route2));
                        }));

        return interconnectedFlights;
    }

    private List<FlightResponse> getInterconnectedFlights(
            final FlightRequest request,
            final int numStops,
            final FlightRoute originRoute,
            final FlightRoute route2) {
        List<FlightResponse> interconnectedFlights = new ArrayList<>();

        // options from origin
        List<FlightLeg> firstLegs = routeFinderService.getFlightsForRoute(request, originRoute);
        // options for second legs
        List<FlightLeg> secondLegs = routeFinderService.getFlightsForRoute(request, route2);

        firstLegs.forEach(firstLeg -> secondLegs.stream()
                .filter(secondLeg -> // 2nd leg needs to be at least 2h after arrival from first leg
                        secondLeg.departureDateTime().isAfter(firstLeg.arrivalDateTime().plusHours(MAX_DELTA_HOURS)))
                .forEach(secondLeg ->
                        interconnectedFlights.add(new FlightResponse(numStops, List.of(firstLeg, secondLeg)))));

        return interconnectedFlights;
    }
}
