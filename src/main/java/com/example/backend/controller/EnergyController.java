package com.example.backend.controller;

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
    public Object getEnergyMix() {
        return energyService.getEnergyMix();
    }

    @GetMapping("/charging-window")
    public Object getBestChargingWindow(@RequestParam int hours) {
        return energyService.getBestChargingWindow(hours);
    }
}