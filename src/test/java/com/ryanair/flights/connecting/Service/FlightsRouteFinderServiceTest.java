package com.ryanair.flights.connecting.Service;

import com.ryanair.flights.connecting.api.FlightLeg;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.domain.FlightScheduleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightsRouteFinderServiceTest {

    private FlightsRouteFinderService unit;

    @Mock
    WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        when(webClientBuilder.baseUrl(ArgumentMatchers.anyString())).thenReturn(webClientBuilder);
        unit = new FlightsRouteFinderService(webClientBuilder);
    }

    @Test
    void buildFlightLegs_withLegInRequest_shouldReturnLeg() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "DEF",
                LocalDateTime.of(2024, 9, 10, 10, 0, 0));
        FlightScheduleResult schedule = new FlightScheduleResult(9, List.of(new FlightScheduleResult.FlightDay(9,
                List.of(new FlightScheduleResult.Flight("1234", "11:00:00", "13:00:00")))));
        // when
        List<FlightLeg> result = unit.buildFlightLegs(request, "ABC", "DEF", schedule, 9);
        // then
        assertEquals(1, result.size());
        assertEquals(new FlightLeg("ABC", "DEF",
                LocalDateTime.of(2024, 9, 9, 11, 0, 0),
                LocalDateTime.of(2024, 9, 9, 13, 0, 0)), result.getFirst());
    }

    @Test
    void buildFlightLegs_withLegArrivalNotInRequest_shouldNotReturnLeg() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "DEF",
                LocalDateTime.of(2024, 9, 10, 10, 0, 0));
        FlightScheduleResult schedule = new FlightScheduleResult(9, List.of(new FlightScheduleResult.FlightDay(10,
                List.of(new FlightScheduleResult.Flight("1234", "09:00:00", "11:00:00")))));
        // when
        List<FlightLeg> result = unit.buildFlightLegs(request, "ABC", "DEF", schedule, 9);
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void buildFlightLegs_withLegDepartureNotInRequest_shouldNotReturnLeg() {
        // given
        FlightRequest request = new FlightRequest("ABC",
                LocalDateTime.of(2024, 9, 9, 10, 0, 0), "DEF",
                LocalDateTime.of(2024, 9, 10, 10, 0, 0));
        FlightScheduleResult schedule = new FlightScheduleResult(9, List.of(new FlightScheduleResult.FlightDay(9,
                List.of(new FlightScheduleResult.Flight("1234", "08:00:00", "11:00:00")))));
        // when
        List<FlightLeg> result = unit.buildFlightLegs(request, "ABC", "DEF", schedule, 9);
        // then
        assertTrue(result.isEmpty());
    }
}