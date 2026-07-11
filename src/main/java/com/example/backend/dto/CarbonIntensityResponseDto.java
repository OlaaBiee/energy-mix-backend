package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CarbonIntensityResponseDto {

    private List<GenerationIntervalDto> data;
}