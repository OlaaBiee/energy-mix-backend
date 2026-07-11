package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnergyMixResponseDto {

    private List<DailyEnergyMixDto> days;
    private String errorMessage;

    public static EnergyMixResponseDto createNewResponse(List<DailyEnergyMixDto> days) {
        EnergyMixResponseDto response = new EnergyMixResponseDto();
        response.setDays(days);
        return response;
    }

    public static EnergyMixResponseDto errorResponse(String errorMessage) {
        EnergyMixResponseDto response = new EnergyMixResponseDto();
        response.setErrorMessage(errorMessage);
        return response;
    }
}