package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEnergyMixDto {

    private String date;
    private int intervalCount;
    private Map<String, Double> averageGenerationMix;
    private double cleanEnergyPercent;
}