package com.example.backend.controller;

import com.example.backend.dto.ChargingWindowResponseDto;
import com.example.backend.dto.EnergyMixResponseDto;
import com.example.backend.service.EnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyService energyService;

    @GetMapping("/test")
    public String test() {
        return "Backend is running";
    }

    @GetMapping("/energy-mix")
    public EnergyMixResponseDto getEnergyMix() {
        return energyService.getEnergyMix();
    }

    @GetMapping("/charging-window")
    public ChargingWindowResponseDto getBestChargingWindow(@RequestParam int hours) {
        return energyService.getBestChargingWindow(hours);
    }
}