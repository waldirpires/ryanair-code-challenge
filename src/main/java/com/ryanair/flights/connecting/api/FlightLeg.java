package com.ryanair.flights.connecting.api;

import java.time.LocalDateTime;

public record FlightLeg (
    String departureAirport,
    String arrivalAirport,
    LocalDateTime departureDateTime,
    LocalDateTime arrivalDateTime){
}
