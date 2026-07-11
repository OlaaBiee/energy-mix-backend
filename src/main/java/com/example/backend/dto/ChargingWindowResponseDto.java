package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargingWindowResponseDto {

    private Integer chargingWindowHours;
    private String start;
    private String end;
    private Double averageCleanEnergyPercent;
    private List<String> cleanEnergySources;
    private String errorMessage;

    public static ChargingWindowResponseDto createNewResponse(
            Integer chargingWindowHours,
            String start,
            String end,
            Double averageCleanEnergyPercent,
            List<String> cleanEnergySources
    ) {
        ChargingWindowResponseDto response = new ChargingWindowResponseDto();
        response.setChargingWindowHours(chargingWindowHours);
        response.setStart(start);
        response.setEnd(end);
        response.setAverageCleanEnergyPercent(averageCleanEnergyPercent);
        response.setCleanEnergySources(cleanEnergySources);
        return response;
    }

    public static ChargingWindowResponseDto errorResponse(String errorMessage) {
        ChargingWindowResponseDto response = new ChargingWindowResponseDto();
        response.setErrorMessage(errorMessage);
        return response;
    }
}