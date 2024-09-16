package com.ryanair.flights.connecting.integration;

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.ryanair.flights.connecting.lambda.FlightLambdaHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ConnectingFlightsIntegrationTest {

    MockLambdaContext lambdaContext = new MockLambdaContext();

    @Test
    void whenTheFlightsPathIsInvokedViaLambda_thenShouldReturnAList() throws IOException {
        FlightLambdaHandler lambdaHandler = new FlightLambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/api/v1/flights", "GET").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);
        Assertions.assertNotNull(resp.getBody());
        Assertions.assertEquals(200, resp.getStatusCode());
    }
}

