package com.ryanair.flights.connecting.domain;

public record FlightRoute (
    String airportFrom,
    String airportTo,
    String connectingAirport,
    String operator) {

    public boolean equalsRequest(String from, String to) {
        return airportFrom().equals(from) && airportTo().equals(to);
    }
}
