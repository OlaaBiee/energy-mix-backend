package com.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EnergyControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testEndpointShouldReturnBackendWorksMessage() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/test", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Backend działa"));
    }

    @Test
    void energyMixShouldReturnDaysAndCleanEnergyPercent() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/energy-mix", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("days"));
        assertTrue(response.getBody().contains("cleanEnergyPercent"));
    }

    @Test
    void chargingWindowShouldReturnStartEndAndAverageCleanEnergyPercent() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/charging-window?hours=3", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("start"));
        assertTrue(response.getBody().contains("end"));
        assertTrue(response.getBody().contains("averageCleanEnergyPercent"));
    }

    @Test
    void chargingWindowShouldRejectZeroHours() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/charging-window?hours=0", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Parametr hours musi"));
    }

    @Test
    void chargingWindowShouldRejectMoreThanSixHours() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/charging-window?hours=7", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Parametr hours musi"));
    }
}