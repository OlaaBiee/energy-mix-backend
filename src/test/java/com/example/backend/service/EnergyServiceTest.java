package com.example.backend.service;

import com.example.backend.client.CarbonIntensityClient;
import com.example.backend.dto.ApiErrorDto;
import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private CarbonIntensityClient carbonIntensityClient;

    @Mock
    private EnergyCalculator energyCalculator;

    @InjectMocks
    private EnergyService energyService;

    @Test
    void shouldReturnErrorForInvalidChargingHours() {
        Object result = energyService.getBestChargingWindow(0);

        assertInstanceOf(ApiErrorDto.class, result);

        ApiErrorDto error = (ApiErrorDto) result;

        assertEquals(
                "Charging time must be a full number of hours from 1 to 6.",
                error.getMessage()
        );

        verifyNoInteractions(carbonIntensityClient);
        verifyNoInteractions(energyCalculator);
    }

    @Test
    void shouldReturnErrorWhenExternalApiReturnsNoDataForEnergyMix() {
        when(carbonIntensityClient.getGenerationData(
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(Collections.emptyList());

        Object result = energyService.getEnergyMix();

        assertInstanceOf(ApiErrorDto.class, result);

        ApiErrorDto error = (ApiErrorDto) result;

        assertEquals("No data received from external API.", error.getMessage());

        verify(energyCalculator, never()).calculateDailyEnergyMix(
                anyList(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldReturnChargingWindowFromCalculator() {
        GenerationIntervalDto interval = new GenerationIntervalDto();
        List<GenerationIntervalDto> intervals = List.of(interval);

        ChargingWindowResponseDto expectedResponse = new ChargingWindowResponseDto(
                1,
                "2026-01-01 01:00",
                "2026-01-01 02:00",
                85.0,
                List.of("biomass", "nuclear", "hydro", "wind", "solar")
        );

        when(carbonIntensityClient.getGenerationData(
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(intervals);

        when(energyCalculator.findBestChargingWindow(
                eq(intervals),
                eq(1),
                any()
        )).thenReturn(Optional.of(expectedResponse));

        Object result = energyService.getBestChargingWindow(1);

        assertSame(expectedResponse, result);
    }
}