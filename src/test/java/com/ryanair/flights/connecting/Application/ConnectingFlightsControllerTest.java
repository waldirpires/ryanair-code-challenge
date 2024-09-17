package com.ryanair.flights.connecting.Application;

import com.ryanair.flights.connecting.Service.ConnectingFlightService;
import com.ryanair.flights.connecting.api.FlightLeg;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.api.FlightResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConnectingFlightsController.class)
class ConnectingFlightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConnectingFlightService service;

    private static final LocalDateTime departureDateTime = LocalDateTime.of(2024, 9, 10, 10, 0, 0);
    private static final LocalDateTime arrivalDateTime = LocalDateTime.of(2024, 9, 10, 11, 0, 0);
    private static final FlightRequest request = new FlightRequest("ABC", departureDateTime, "DEF", arrivalDateTime);
    private static final FlightResponse response = new FlightResponse(0, List.of(
            new FlightLeg("ABC", "DEF", departureDateTime, arrivalDateTime)));
    private static final FlightResponse emptyResponse = new FlightResponse(0, List.of());

    @Test
    void getConnectingFlights_forEmptyResult_shouldReturnExpected() throws Exception {
        // given
        Mockito.when(service.findFlights(request)).thenReturn(List.of(emptyResponse));
        // when/then
        this.mockMvc.perform(get("/api/v1/flights")
                        .queryParam("departure", "ABC")
                        .queryParam("departureDateTime", departureDateTime.toString())
                        .queryParam("arrival", "DEF")
                        .queryParam("arrivalDateTime", arrivalDateTime.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].stops", is(0)))
                .andExpect(jsonPath("$[0].legs.length()", is(0)));
    }

    @Test
    void getConnectingFlights_forNonEmptyResult_shouldReturnExpected() throws Exception {
        // given
        Mockito.when(service.findFlights(request)).thenReturn(List.of(response));
        // when/then
        this.mockMvc.perform(get("/api/v1/flights")
                        .queryParam("departure", "ABC")
                        .queryParam("departureDateTime", departureDateTime.toString())
                        .queryParam("arrival", "DEF")
                        .queryParam("arrivalDateTime", arrivalDateTime.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].stops", is(0)))
                .andExpect(jsonPath("$[0].legs.length()", is(1)))
                .andExpect(jsonPath("$[0].legs[0].departureAirport", is("ABC")))
                .andExpect(jsonPath("$[0].legs[0].arrivalAirport", is("DEF")))
                .andExpect(jsonPath("$[0].legs[0].departureDateTime", is("2024-09-10 10:00:00")))
                .andExpect(jsonPath("$[0].legs[0].arrivalDateTime", is("2024-09-10 11:00:00")));
    }
}