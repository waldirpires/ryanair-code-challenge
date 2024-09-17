package com.ryanair.flights.connecting;

import com.ryanair.flights.connecting.Service.ConnectingFlightService;
import com.ryanair.flights.connecting.api.FlightRequest;
import com.ryanair.flights.connecting.api.FlightResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.Function;

@SpringBootApplication
public class ConnectingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectingApplication.class, args);
	}

	/**
	 * Bean representing the Lambda function to find flights.
	 */
	@Bean
	public Function<FlightRequest, List<FlightResponse>> findFlights(ConnectingFlightService flightService) {
		return flightService::findFlights;
	}
}
