package com.ryanair.flights.connecting.api;

import java.time.LocalDateTime;

public record FlightRequest (
    String departure,
    LocalDateTime departureDateTime,
    String arrival,
    LocalDateTime arrivalDateTime){
}
