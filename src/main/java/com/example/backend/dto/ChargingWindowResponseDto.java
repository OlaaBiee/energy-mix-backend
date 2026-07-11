package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingWindowResponseDto {

    private int chargingWindowHours;
    private String start;
    private String end;
    private double averageCleanEnergyPercent;
    private List<String> cleanEnergySources;
}