package com.example.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
public class EnergyController {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.carbonintensity.org.uk/generation/";

    private static final List<String> CLEAN_ENERGY_SOURCES = List.of(
            "biomass", "nuclear", "hydro", "wind", "solar"
    );

    private static final List<String> ALL_ENERGY_SOURCES = List.of(
            "biomass", "coal", "imports", "gas", "nuclear", "other", "hydro", "solar", "wind"
    );

    @GetMapping("/api/test")
    public String test() {
        return "Backend działa";
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/api/energy-mix")
    public Map<String, Object> getEnergyMix() {
        ZoneId londonZone = ZoneId.of("Europe/London");

        LocalDate today = LocalDate.now(londonZone);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);

        ZonedDateTime start = today.atStartOfDay(londonZone);
        ZonedDateTime end = today.plusDays(3).atStartOfDay(londonZone);

        String from = formatForApi(start);
        String to = formatForApi(end);

        String url = API_URL + from + "/" + to;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || response.get("data") == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Brak danych z API");
            return error;
        }

        List<Map<String, Object>> intervals =
                (List<Map<String, Object>>) response.get("data");

        Map<LocalDate, List<Map<String, Object>>> groupedByDate = new LinkedHashMap<>();

        groupedByDate.put(today, new ArrayList<>());
        groupedByDate.put(tomorrow, new ArrayList<>());
        groupedByDate.put(dayAfterTomorrow, new ArrayList<>());

        for (Map<String, Object> interval : intervals) {
            String intervalFrom = (String) interval.get("from");

            LocalDate intervalDate = OffsetDateTime.parse(intervalFrom)
                    .atZoneSameInstant(londonZone)
                    .toLocalDate();

            if (groupedByDate.containsKey(intervalDate)) {
                groupedByDate.get(intervalDate).add(interval);
            }
        }

        List<Map<String, Object>> days = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : groupedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Map<String, Object>> dailyIntervals = entry.getValue();

            Map<String, Double> fuelSums = new LinkedHashMap<>();

            for (String fuel : ALL_ENERGY_SOURCES) {
                fuelSums.put(fuel, 0.0);
            }

            double cleanEnergySum = 0.0;

            for (Map<String, Object> interval : dailyIntervals) {
                List<Map<String, Object>> generationMix =
                        (List<Map<String, Object>>) interval.get("generationmix");

                double cleanEnergyInInterval = 0.0;

                for (Map<String, Object> source : generationMix) {
                    String fuel = (String) source.get("fuel");
                    Number percentageNumber = (Number) source.get("perc");
                    double percentage = percentageNumber.doubleValue();

                    fuelSums.put(fuel, fuelSums.getOrDefault(fuel, 0.0) + percentage);

                    if (CLEAN_ENERGY_SOURCES.contains(fuel)) {
                        cleanEnergyInInterval += percentage;
                    }
                }

                cleanEnergySum += cleanEnergyInInterval;
            }

            Map<String, Double> averageGenerationMix = new LinkedHashMap<>();

            for (String fuel : ALL_ENERGY_SOURCES) {
                double average = dailyIntervals.isEmpty()
                        ? 0.0
                        : fuelSums.get(fuel) / dailyIntervals.size();

                averageGenerationMix.put(fuel, round(average));
            }

            double cleanEnergyPercent = dailyIntervals.isEmpty()
                    ? 0.0
                    : cleanEnergySum / dailyIntervals.size();

            Map<String, Object> dayResult = new LinkedHashMap<>();

            dayResult.put("date", date.toString());
            dayResult.put("intervalCount", dailyIntervals.size());
            dayResult.put("averageGenerationMix", averageGenerationMix);
            dayResult.put("cleanEnergyPercent", round(cleanEnergyPercent));

            days.add(dayResult);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days);

        return result;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/api/charging-window")
    public Map<String, Object> getBestChargingWindow(@RequestParam int hours) {
        if (hours < 1 || hours > 6) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Parametr hours musi być liczbą całkowitą od 1 do 6.");
            return error;
        }

        ZoneId londonZone = ZoneId.of("Europe/London");

        ZonedDateTime start = roundUpToNextHalfHour(ZonedDateTime.now(londonZone));
        ZonedDateTime end = start.plusDays(2);

        String from = formatForApi(start);
        String to = formatForApi(end);

        String url = API_URL + from + "/" + to;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || response.get("data") == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Brak danych z API");
            return error;
        }

        List<Map<String, Object>> intervals =
                (List<Map<String, Object>>) response.get("data");

        List<Map<String, Object>> preparedIntervals = new ArrayList<>();

        for (Map<String, Object> interval : intervals) {
            List<Map<String, Object>> generationMix =
                    (List<Map<String, Object>>) interval.get("generationmix");

            double cleanEnergyPercent = calculateCleanEnergyPercent(generationMix);

            Map<String, Object> preparedInterval = new LinkedHashMap<>();
            preparedInterval.put("from", interval.get("from"));
            preparedInterval.put("to", interval.get("to"));
            preparedInterval.put("cleanEnergyPercent", cleanEnergyPercent);

            preparedIntervals.add(preparedInterval);
        }

        preparedIntervals.sort(Comparator.comparing(interval ->
                OffsetDateTime.parse((String) interval.get("from"))
        ));

        int windowSize = hours * 2;

        if (preparedIntervals.size() < windowSize) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Za mało danych do wyznaczenia okna ładowania.");
            return error;
        }

        Map<String, Object> bestWindow = null;
        double bestAverage = -1.0;

        for (int i = 0; i <= preparedIntervals.size() - windowSize; i++) {
            List<Map<String, Object>> currentWindow =
                    preparedIntervals.subList(i, i + windowSize);

            if (!isWindowContinuous(currentWindow)) {
                continue;
            }

            double sum = 0.0;

            for (Map<String, Object> interval : currentWindow) {
                sum += (double) interval.get("cleanEnergyPercent");
            }

            double average = sum / currentWindow.size();

            if (average > bestAverage) {
                bestAverage = average;
                bestWindow = new LinkedHashMap<>();
                bestWindow.put("start", currentWindow.get(0).get("from"));
                bestWindow.put("end", currentWindow.get(currentWindow.size() - 1).get("to"));
                bestWindow.put("averageCleanEnergyPercent", round(average));
            }
        }

        if (bestWindow == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Nie znaleziono ciągłego okna ładowania.");
            return error;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chargingWindowHours", hours);
        result.put("start", formatDateTimeForResponse((String) bestWindow.get("start"), londonZone));
        result.put("end", formatDateTimeForResponse((String) bestWindow.get("end"), londonZone));
        result.put("averageCleanEnergyPercent", bestWindow.get("averageCleanEnergyPercent"));
        result.put("cleanEnergySources", CLEAN_ENERGY_SOURCES);

        return result;
    }

    private double calculateCleanEnergyPercent(List<Map<String, Object>> generationMix) {
        double cleanEnergyPercent = 0.0;

        for (Map<String, Object> source : generationMix) {
            String fuel = (String) source.get("fuel");
            Number percentageNumber = (Number) source.get("perc");
            double percentage = percentageNumber.doubleValue();

            if (CLEAN_ENERGY_SOURCES.contains(fuel)) {
                cleanEnergyPercent += percentage;
            }
        }

        return cleanEnergyPercent;
    }

    private boolean isWindowContinuous(List<Map<String, Object>> window) {
        for (int i = 1; i < window.size(); i++) {
            String previousEnd = (String) window.get(i - 1).get("to");
            String currentStart = (String) window.get(i).get("from");

            if (!previousEnd.equals(currentStart)) {
                return false;
            }
        }

        return true;
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

    private String formatForApi(ZonedDateTime dateTime) {
        return dateTime
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
    }

    private String formatDateTimeForResponse(String dateTime, ZoneId zoneId) {
        return OffsetDateTime.parse(dateTime)
                .atZoneSameInstant(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}