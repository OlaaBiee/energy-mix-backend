package com.example.backend.service;

import com.example.backend.client.CarbonIntensityClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnergyService {

    private final CarbonIntensityClient carbonIntensityClient;
    private final EnergyCalculator energyCalculator;

    public EnergyService(CarbonIntensityClient carbonIntensityClient, EnergyCalculator energyCalculator) {
        this.carbonIntensityClient = carbonIntensityClient;
        this.energyCalculator = energyCalculator;
    }

    public Map<String, Object> getEnergyMix() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        LocalDate today = LocalDate.now(londonZone);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        ZonedDateTime start = today.atStartOfDay(londonZone);
        ZonedDateTime end = today.plusDays(3).atStartOfDay(londonZone);

        List<Map<String, Object>> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return createError("Brak danych z API");
        }

        return energyCalculator.calculateDailyEnergyMix(
                intervals,
                today,
                tomorrow,
                dayAfterTomorrow,
                londonZone
        );
    }

    public Map<String, Object> getBestChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            return createError("Parametr hours musi być liczbą całkowitą od 1 do 6.");
        }

        ZoneId londonZone = ZoneId.of("Europe/London");

        ZonedDateTime start = roundUpToNextHalfHour(ZonedDateTime.now(londonZone));
        ZonedDateTime end = start.plusDays(2);

        List<Map<String, Object>> intervals =
                carbonIntensityClient.getGenerationData(start, end);

        if (intervals.isEmpty()) {
            return createError("Brak danych z API");
        }

        List<Map<String, Object>> preparedIntervals =
                energyCalculator.prepareChargingIntervals(intervals);

        return energyCalculator.findBestChargingWindow(
                preparedIntervals,
                hours,
                londonZone
        );
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

    private Map<String, Object> createError(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", message);
        return error;
    }
}