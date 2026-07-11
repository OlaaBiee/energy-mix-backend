package com.example.backend.service;

import com.example.backend.client.CarbonIntensityClient;
import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.EnergyMixResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private static final int MIN_CHARGING_HOURS = 1;
    private static final int MAX_CHARGING_HOURS = 6;
    private static final int ENERGY_MIX_DAYS = 3;
    private static final int CHARGING_FORECAST_DAYS = 2;

    private final CarbonIntensityClient carbonIntensityClient;
    private final EnergyCalculator energyCalculator;

    public EnergyMixResponseDto getEnergyMix() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        LocalDate today = LocalDate.now(londonZone);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        ZonedDateTime start = today.atStartOfDay(londonZone);
        ZonedDateTime end = today.plusDays(ENERGY_MIX_DAYS).atStartOfDay(londonZone);

        List<GenerationIntervalDto> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return EnergyMixResponseDto.errorResponse("No data received from external API.");
        }

        return energyCalculator.calculateDailyEnergyMix(
                intervals,
                today,
                tomorrow,
                dayAfterTomorrow,
                londonZone
        );
    }

    public ChargingWindowResponseDto getBestChargingWindow(int hours) {
        if (hours < MIN_CHARGING_HOURS || hours > MAX_CHARGING_HOURS) {
            return ChargingWindowResponseDto.errorResponse(
                    "Charging time must be a full number of hours from 1 to 6."
            );
        }

        ZoneId londonZone = ZoneId.of("Europe/London");

        ZonedDateTime start = roundUpToNextHalfHour(ZonedDateTime.now(londonZone));
        ZonedDateTime end = start.plusDays(CHARGING_FORECAST_DAYS);

        List<GenerationIntervalDto> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return ChargingWindowResponseDto.errorResponse("No data received from external API.");
        }

        return energyCalculator.findBestChargingWindow(intervals, hours, londonZone)
                .orElse(ChargingWindowResponseDto.errorResponse("No continuous charging window found."));
    }

    private ZonedDateTime roundUpToNextHalfHour(ZonedDateTime dateTime) {
        ZonedDateTime result = dateTime.withSecond(0).withNano(0);

        int minute = result.getMinute();

        if (minute == 0 || minute == 30) {
            return result;
        }

        if (minute < 30) {
            return result.withMinute(30);
        }

        return result.plusHours(1).withMinute(0);
    }
}