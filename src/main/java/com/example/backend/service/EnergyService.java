package com.example.backend.service;

import com.example.backend.client.CarbonIntensityClient;
import com.example.backend.dto.ApiErrorDto;
import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.EnergyMixResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnergyService {

    private final CarbonIntensityClient carbonIntensityClient;
    private final EnergyCalculator energyCalculator;

    public EnergyService(CarbonIntensityClient carbonIntensityClient, EnergyCalculator energyCalculator) {
        this.carbonIntensityClient = carbonIntensityClient;
        this.energyCalculator = energyCalculator;
    }

    public Object getEnergyMix() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        LocalDate today = LocalDate.now(londonZone);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        ZonedDateTime start = today.atStartOfDay(londonZone);
        ZonedDateTime end = today.plusDays(3).atStartOfDay(londonZone);

        List<GenerationIntervalDto> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return new ApiErrorDto("No data received from external API.");
        }

        EnergyMixResponseDto response = energyCalculator.calculateDailyEnergyMix(
                intervals,
                today,
                tomorrow,
                dayAfterTomorrow,
                londonZone
        );

        return response;
    }

    public Object getBestChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            return new ApiErrorDto("Charging time must be a full number of hours from 1 to 6.");
        }

        ZoneId londonZone = ZoneId.of("Europe/London");

        ZonedDateTime start = roundUpToNextHalfHour(ZonedDateTime.now(londonZone));
        ZonedDateTime end = start.plusDays(2);

        List<GenerationIntervalDto> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return new ApiErrorDto("No data received from external API.");
        }

        Optional<ChargingWindowResponseDto> chargingWindow =
                energyCalculator.findBestChargingWindow(intervals, hours, londonZone);

        if (chargingWindow.isEmpty()) {
            return new ApiErrorDto("No continuous charging window found.");
        }

        return chargingWindow.get();
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