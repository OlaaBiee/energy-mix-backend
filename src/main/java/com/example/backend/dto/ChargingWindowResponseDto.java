package com.example.backend.dto;

import java.util.List;

public class ChargingWindowResponseDto {

    private int chargingWindowHours;
    private String start;
    private String end;
    private double averageCleanEnergyPercent;
    private List<String> cleanEnergySources;

    public ChargingWindowResponseDto(
            int chargingWindowHours,
            String start,
            String end,
            double averageCleanEnergyPercent,
            List<String> cleanEnergySources
    ) {
        this.chargingWindowHours = chargingWindowHours;
        this.start = start;
        this.end = end;
        this.averageCleanEnergyPercent = averageCleanEnergyPercent;
        this.cleanEnergySources = cleanEnergySources;
    }

    public int getChargingWindowHours() {
        return chargingWindowHours;
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

    public List<String> getCleanEnergySources() {
        return cleanEnergySources;
    }
}