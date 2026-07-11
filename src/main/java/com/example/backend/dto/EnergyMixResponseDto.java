package com.example.backend.dto;

import java.util.List;

public class EnergyMixResponseDto {

    private List<DailyEnergyMixDto> days;

    public EnergyMixResponseDto(List<DailyEnergyMixDto> days) {
        this.days = days;
    }

    public List<DailyEnergyMixDto> getDays() {
        return days;
    }

    public void setDays(List<DailyEnergyMixDto> days) {
        this.days = days;
    }
}