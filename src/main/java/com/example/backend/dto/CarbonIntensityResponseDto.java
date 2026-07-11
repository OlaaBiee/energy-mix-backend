package com.example.backend.dto;

import java.util.List;

public class CarbonIntensityResponseDto {

    private List<GenerationIntervalDto> data;

    public List<GenerationIntervalDto> getData() {
        return data;
    }

    public void setData(List<GenerationIntervalDto> data) {
        this.data = data;
    }
}
