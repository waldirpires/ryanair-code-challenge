package com.ryanair.flights.connecting.Service;

import com.ryanair.flights.connecting.api.FlightLeg;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.api.FlightResponse;
import com.ryanair.flights.connecting.domain.FlightRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectingFlightServiceTest {

    private ConnectingFlightService unit;

    @Mock
    private FlightsRouteFinderService flightsRouteFinderService;

    @BeforeEach
    public void setup() {
        unit = new ConnectingFlightService(flightsRouteFinderService);
    }

    @Test
    void findDirectFlights_withFlightsAvailable_shouldReturnFlightLegs() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "DEF",
                LocalDateTime.of(2024, 9, 10, 10, 0, 0));
        FlightRoute route = new FlightRoute("ABC", "DEF", "HIJ", "TAP");
        List<FlightRoute> routes = List.of(route);

        when(flightsRouteFinderService.getFlightsForRoute(request, route)).thenReturn(
                List.of(new FlightLeg("ABC", "DEF",
                        LocalDateTime.of(2024, 9, 9, 10, 0, 0),
                        LocalDateTime.of(2024, 9, 9, 11, 0, 0)))
        );

        // when
        List<FlightResponse> result = unit.findDirectFlights(request, routes);
        // then
        assertEquals(1, result.size());
    }

    // ABC -> DEF -> GHI
    @Test
    void findInterconnectedFlights_WithLegsAvailable_shouldReturnResponseWithLegs() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "GHI",
                LocalDateTime.of(2024, 9, 10, 13, 0, 0));
        FlightRoute route = new FlightRoute("ABC", "GHI", "DEF", "RYA");
        FlightRoute route3 = new FlightRoute("ABC", "DEF", "", "RYA");
        FlightRoute route2 = new FlightRoute("DEF", "GHI", "", "RYA");
        List<FlightRoute> routes = List.of(route, route2, route3);

        FlightLeg leg1 = new FlightLeg("ABC", "DEF",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0),
                LocalDateTime.of(2024, 9, 9, 11, 0, 0));
        when(flightsRouteFinderService.getFlightsForRoute(request, route3)).thenReturn(List.of(leg1));

        FlightLeg leg2 = new FlightLeg("DEF", "GHI",
                LocalDateTime.of(2024, 9, 9, 14, 0, 0),
                LocalDateTime.of(2024, 9, 9, 15, 0, 0));
        when(flightsRouteFinderService.getFlightsForRoute(request, route2)).thenReturn(List.of(leg2));

        // when
        List<FlightResponse> result = unit.findInterconnectedFlights(request, routes, 1);
        // then
        assertEquals(1, result.size());
        FlightResponse response = result.getFirst();
        assertEquals(1, response.stops());
        assertEquals(2, response.legs().size());
        assertEquals(leg1, response.legs().get(0));
        assertEquals(leg2, response.legs().get(1));
    }

    @Test
    void findInterconnectedFlights_WithLegsAvailableButUnder2h_shouldReturnResponseWithNoLegs() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "GHI",
                LocalDateTime.of(2024, 9, 10, 13, 0, 0));
        FlightRoute route = new FlightRoute("ABC", "GHI", "DEF", "RYA");
        FlightRoute route3 = new FlightRoute("ABC", "DEF", "", "RYA");
        FlightRoute route2 = new FlightRoute("DEF", "GHI", "", "RYA");
        List<FlightRoute> routes = List.of(route, route2, route3);

        FlightLeg leg1 = new FlightLeg("ABC", "DEF",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0),
                LocalDateTime.of(2024, 9, 9, 11, 0, 0));
        when(flightsRouteFinderService.getFlightsForRoute(request, route3)).thenReturn(List.of(leg1));

        FlightLeg leg2 = new FlightLeg("DEF", "GHI",
                LocalDateTime.of(2024, 9, 9, 13, 0, 0),
                LocalDateTime.of(2024, 9, 9, 15, 0, 0));
        when(flightsRouteFinderService.getFlightsForRoute(request, route2)).thenReturn(List.of(leg2));

        // when
        List<FlightResponse> result = unit.findInterconnectedFlights(request, routes, 1);
        // then
        assertEquals(0, result.size());
    }

    @Test
    void findInterconnectedFlights_WithNoLegsAvailable_shouldReturnResponseWithNoLegs() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "GHI",
                LocalDateTime.of(2024, 9, 10, 13, 0, 0));
        FlightRoute route = new FlightRoute("KLM", "GHI", "DEF", "RYA");
        FlightRoute route3 = new FlightRoute("MNR", "DEF", "", "RYA");
        FlightRoute route2 = new FlightRoute("RST", "GHI", "", "RYA");
        List<FlightRoute> routes = List.of(route, route2, route3);

        // when
        List<FlightResponse> result = unit.findInterconnectedFlights(request, routes, 1);
        // then
        assertEquals(0, result.size());
        // verify
        verifyNoInteractions(flightsRouteFinderService);
    }
}