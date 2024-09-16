package com.ryanair.flights.connecting.Application;

import com.ryanair.flights.connecting.Service.ConnectingFlightService;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.api.FlightResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ConnectingFlightsController {

    @Autowired
    private ConnectingFlightService service;

    @GetMapping("/flights")
    public List<FlightResponse> getConnectingFlights(
            @RequestParam String departure,
            @RequestParam LocalDateTime departureDateTime,
            @RequestParam String arrival,
            @RequestParam LocalDateTime arrivalDateTime) {
        FlightRequest request = new FlightRequest(departure, departureDateTime, arrival, arrivalDateTime);
        log.info("GET connecting flights with params: {}", request);
        List<FlightResponse> response = service.findFlights(request);

        log.info("GET response: {}", response);
        return response;
    }
}
