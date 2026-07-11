package com.example.backend.service;


import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.DailyEnergyMixDto;
import com.example.backend.dto.EnergyMixResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import com.example.backend.dto.GenerationMixDto;
import com.example.backend.model.EnergySource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EnergyCalculator {

    private static final int INTERVALS_PER_HOUR = 2;
    private static final double INITIAL_BEST_AVERAGE = -1.0;
    private static final double ROUNDING_FACTOR = 100.0;

    public EnergyMixResponseDto calculateDailyEnergyMix(
            List<GenerationIntervalDto> intervals,
            LocalDate today,
            LocalDate tomorrow,
            LocalDate dayAfterTomorrow,
            ZoneId londonZone
    ) {
        Map<LocalDate, List<GenerationIntervalDto>> groupedByDate = new LinkedHashMap<>();

        groupedByDate.put(today, new ArrayList<>());
        groupedByDate.put(tomorrow, new ArrayList<>());
        groupedByDate.put(dayAfterTomorrow, new ArrayList<>());

        for (GenerationIntervalDto interval : intervals) {
            LocalDate intervalDate = OffsetDateTime.parse(interval.getFrom())
                    .atZoneSameInstant(londonZone)
                    .toLocalDate();

            if (groupedByDate.containsKey(intervalDate)) {
                groupedByDate.get(intervalDate).add(interval);
            }
        }

        List<DailyEnergyMixDto> days = new ArrayList<>();

        for (Map.Entry<LocalDate, List<GenerationIntervalDto>> entry : groupedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<GenerationIntervalDto> dailyIntervals = entry.getValue();

            Map<String, Double> fuelSums = new LinkedHashMap<>();

            for (String fuel : EnergySource.getAllNames()) {
                fuelSums.put(fuel, 0.0);
            }

            double cleanEnergySum = 0.0;

            for (GenerationIntervalDto interval : dailyIntervals) {
                double cleanEnergyInInterval = 0.0;

                for (GenerationMixDto source : interval.getGenerationmix()) {
                    String fuel = source.getFuel();
                    double percentage = source.getPerc();

                    fuelSums.put(fuel, fuelSums.getOrDefault(fuel, 0.0) + percentage);

                    if (EnergySource.isCleanEnergySource(fuel)) {
                        cleanEnergyInInterval += percentage;
                    }
                }

                cleanEnergySum += cleanEnergyInInterval;
            }

            Map<String, Double> averageGenerationMix = new LinkedHashMap<>();

            for (String fuel : EnergySource.getAllNames()) {
                double average = dailyIntervals.isEmpty()
                        ? 0.0
                        : fuelSums.get(fuel) / dailyIntervals.size();

                averageGenerationMix.put(fuel, round(average));
            }

            double cleanEnergyPercent = dailyIntervals.isEmpty()
                    ? 0.0
                    : cleanEnergySum / dailyIntervals.size();

            DailyEnergyMixDto dayResult = new DailyEnergyMixDto(
                    date.toString(),
                    dailyIntervals.size(),
                    averageGenerationMix,
                    round(cleanEnergyPercent)
            );

            days.add(dayResult);
        }

        return EnergyMixResponseDto.createNewResponse(days);
    }

    public Optional<ChargingWindowResponseDto> findBestChargingWindow(
            List<GenerationIntervalDto> intervals,
            int hours,
            ZoneId londonZone
    ) {
        List<PreparedChargingInterval> preparedIntervals = prepareChargingIntervals(intervals);

        int windowSize = hours * INTERVALS_PER_HOUR;

        if (preparedIntervals.size() < windowSize) {
            return Optional.empty();
        }

        PreparedChargingWindow bestWindow = null;
        double bestAverage = INITIAL_BEST_AVERAGE;

        for (int i = 0; i <= preparedIntervals.size() - windowSize; i++) {
            List<PreparedChargingInterval> currentWindow =
                    preparedIntervals.subList(i, i + windowSize);

            if (!isWindowContinuous(currentWindow)) {
                continue;
            }

            double sum = 0.0;

            for (PreparedChargingInterval interval : currentWindow) {
                sum += interval.getCleanEnergyPercent();
            }

            double average = sum / currentWindow.size();

            if (average > bestAverage) {
                bestAverage = average;

                bestWindow = new PreparedChargingWindow(
                        currentWindow.get(0).getFrom(),
                        currentWindow.get(currentWindow.size() - 1).getTo(),
                        round(average)
                );
            }
        }

        if (bestWindow == null) {
            return Optional.empty();
        }

        ChargingWindowResponseDto response = ChargingWindowResponseDto.createNewResponse(
                hours,
                formatDateTimeForResponse(bestWindow.getStart(), londonZone),
                formatDateTimeForResponse(bestWindow.getEnd(), londonZone),
                bestWindow.getAverageCleanEnergyPercent(),
                EnergySource.getCleanEnergyNames()
        );

        return Optional.of(response);
    }

    private List<PreparedChargingInterval> prepareChargingIntervals(List<GenerationIntervalDto> intervals) {
        List<PreparedChargingInterval> preparedIntervals = new ArrayList<>();

        for (GenerationIntervalDto interval : intervals) {
            double cleanEnergyPercent = calculateCleanEnergyPercent(interval.getGenerationmix());

            PreparedChargingInterval preparedInterval = new PreparedChargingInterval(
                    interval.getFrom(),
                    interval.getTo(),
                    cleanEnergyPercent
            );

            preparedIntervals.add(preparedInterval);
        }

        preparedIntervals.sort(Comparator.comparing(interval ->
                OffsetDateTime.parse(interval.getFrom())
        ));

        return preparedIntervals;
    }

    private double calculateCleanEnergyPercent(List<GenerationMixDto> generationMix) {
        double cleanEnergyPercent = 0.0;

        for (GenerationMixDto source : generationMix) {
            if (EnergySource.isCleanEnergySource(source.getFuel())) {
                cleanEnergyPercent += source.getPerc();
            }
        }

        return cleanEnergyPercent;
    }

    private boolean isWindowContinuous(List<PreparedChargingInterval> window) {
        for (int i = 1; i < window.size(); i++) {
            String previousEnd = window.get(i - 1).getTo();
            String currentStart = window.get(i).getFrom();

            if (!previousEnd.equals(currentStart)) {
                return false;
            }
        }

        return true;
    }

    private String formatDateTimeForResponse(String dateTime, ZoneId zoneId) {
        return OffsetDateTime.parse(dateTime)
                .atZoneSameInstant(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private double round(double value) {
        return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }

    private static class PreparedChargingInterval {

        private final String from;
        private final String to;
        private final double cleanEnergyPercent;

        private PreparedChargingInterval(String from, String to, double cleanEnergyPercent) {
            this.from = from;
            this.to = to;
            this.cleanEnergyPercent = cleanEnergyPercent;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public double getCleanEnergyPercent() {
            return cleanEnergyPercent;
        }
    }

    private static class PreparedChargingWindow {

        private final String start;
        private final String end;
        private final double averageCleanEnergyPercent;

        private PreparedChargingWindow(String start, String end, double averageCleanEnergyPercent) {
            this.start = start;
            this.end = end;
            this.averageCleanEnergyPercent = averageCleanEnergyPercent;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

        public double getAverageCleanEnergyPercent() {
            return averageCleanEnergyPercent;
        }
    }
}