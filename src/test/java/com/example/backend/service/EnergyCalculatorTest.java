package com.example.backend.service;

import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.DailyEnergyMixDto;
import com.example.backend.dto.EnergyMixResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import com.example.backend.dto.GenerationMixDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyCalculatorTest {

    private final EnergyCalculator energyCalculator = new EnergyCalculator();

    @Test
    void shouldCalculateDailyEnergyMixAverages() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        LocalDate today = LocalDate.of(2026, 1, 1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        List<GenerationIntervalDto> intervals = List.of(
                interval(
                        "2026-01-01T00:00Z",
                        "2026-01-01T00:30Z",
                        mix("biomass", 10.0),
                        mix("wind", 20.0),
                        mix("gas", 70.0)
                ),
                interval(
                        "2026-01-01T00:30Z",
                        "2026-01-01T01:00Z",
                        mix("biomass", 20.0),
                        mix("wind", 30.0),
                        mix("gas", 50.0)
                )
        );

        EnergyMixResponseDto response = energyCalculator.calculateDailyEnergyMix(
                intervals,
                today,
                tomorrow,
                dayAfterTomorrow,
                londonZone
        );

        assertEquals(3, response.getDays().size());

        DailyEnergyMixDto firstDay = response.getDays().get(0);

        assertEquals("2026-01-01", firstDay.getDate());
        assertEquals(2, firstDay.getIntervalCount());
        assertEquals(40.0, firstDay.getCleanEnergyPercent(), 0.001);

        Map<String, Double> averageMix = firstDay.getAverageGenerationMix();

        assertEquals(15.0, averageMix.get("biomass"), 0.001);
        assertEquals(25.0, averageMix.get("wind"), 0.001);
        assertEquals(60.0, averageMix.get("gas"), 0.001);
    }

    @Test
    void shouldFindBestChargingWindow() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        List<GenerationIntervalDto> intervals = List.of(
                interval(
                        "2026-01-01T00:00Z",
                        "2026-01-01T00:30Z",
                        mix("wind", 10.0),
                        mix("gas", 90.0)
                ),
                interval(
                        "2026-01-01T00:30Z",
                        "2026-01-01T01:00Z",
                        mix("wind", 20.0),
                        mix("gas", 80.0)
                ),
                interval(
                        "2026-01-01T01:00Z",
                        "2026-01-01T01:30Z",
                        mix("wind", 90.0),
                        mix("gas", 10.0)
                ),
                interval(
                        "2026-01-01T01:30Z",
                        "2026-01-01T02:00Z",
                        mix("wind", 80.0),
                        mix("gas", 20.0)
                )
        );

        Optional<ChargingWindowResponseDto> response =
                energyCalculator.findBestChargingWindow(intervals, 1, londonZone);

        assertTrue(response.isPresent());

        ChargingWindowResponseDto result = response.get();

        assertEquals(1, result.getChargingWindowHours());
        assertEquals("2026-01-01 01:00", result.getStart());
        assertEquals("2026-01-01 02:00", result.getEnd());
        assertEquals(85.0, result.getAverageCleanEnergyPercent(), 0.001);
    }

    @Test
    void shouldReturnEmptyWhenThereIsNotEnoughDataForChargingWindow() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        List<GenerationIntervalDto> intervals = List.of(
                interval(
                        "2026-01-01T00:00Z",
                        "2026-01-01T00:30Z",
                        mix("wind", 50.0),
                        mix("gas", 50.0)
                )
        );

        Optional<ChargingWindowResponseDto> response =
                energyCalculator.findBestChargingWindow(intervals, 2, londonZone);

        assertTrue(response.isEmpty());
    }

    private GenerationIntervalDto interval(String from, String to, GenerationMixDto... generationMix) {
        GenerationIntervalDto interval = new GenerationIntervalDto();

        interval.setFrom(from);
        interval.setTo(to);
        interval.setGenerationmix(List.of(generationMix));

        return interval;
    }

    private GenerationMixDto mix(String fuel, double percentage) {
        GenerationMixDto generationMix = new GenerationMixDto();

        generationMix.setFuel(fuel);
        generationMix.setPerc(percentage);

        return generationMix;
    }
}