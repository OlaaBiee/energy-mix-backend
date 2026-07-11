package com.example.backend.controller;

import com.example.backend.service.EnergyService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/api/test")
    public String test() {
        return "Backend is running";
    }

    @GetMapping("/api/energy-mix")
    public Object getEnergyMix() {
        return energyService.getEnergyMix();
    }

    @GetMapping("/api/charging-window")
    public Object getBestChargingWindow(@RequestParam int hours) {
        return energyService.getBestChargingWindow(hours);
    }
}