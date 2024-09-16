package com.ryanair.flights.connecting.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record FlightResponse (
    int stops,
    List<FlightLeg> legs){

    public FlightResponse {
        legs = Optional.ofNullable(legs).orElse(Collections.emptyList());
    }
}
