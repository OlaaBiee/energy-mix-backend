package com.example.backend.dto;

import java.util.Map;

public class DailyEnergyMixDto {

    private String date;
    private int intervalCount;
    private Map<String, Double> averageGenerationMix;
    private double cleanEnergyPercent;

    public DailyEnergyMixDto(
            String date,
            int intervalCount,
            Map<String, Double> averageGenerationMix,
            double cleanEnergyPercent
    ) {
        this.date = date;
        this.intervalCount = intervalCount;
        this.averageGenerationMix = averageGenerationMix;
        this.cleanEnergyPercent = cleanEnergyPercent;
    }

    public String getDate() {
        return date;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public Map<String, Double> getAverageGenerationMix() {
        return averageGenerationMix;
    }

    public double getCleanEnergyPercent() {
        return cleanEnergyPercent;
    }
}