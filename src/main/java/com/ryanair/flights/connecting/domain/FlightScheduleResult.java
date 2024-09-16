package com.ryanair.flights.connecting.domain;

import java.util.List;

public record FlightScheduleResult(
    int month,
    List<FlightDay> flightDays) {

    public record FlightDay (
        int day,
        List<Flight> flights) {
    }

    public record Flight(
        String number,
        String departureTime,
        String arrivalTime){
    }
}
