package com.example.backend.model;

import java.util.Arrays;
import java.util.List;

public enum EnergySource {

    BIOMASS("biomass", true),
    COAL("coal", false),
    IMPORTS("imports", false),
    GAS("gas", false),
    NUCLEAR("nuclear", true),
    OTHER("other", false),
    HYDRO("hydro", true),
    SOLAR("solar", true),
    WIND("wind", true);

    private final String name;
    private final boolean cleanEnergy;

    EnergySource(String name, boolean cleanEnergy) {
        this.name = name;
        this.cleanEnergy = cleanEnergy;
    }

    public String getName() {
        return name;
    }

    public boolean isCleanEnergy() {
        return cleanEnergy;
    }

    public static List<String> getAllNames() {
        return Arrays.stream(values())
                .map(EnergySource::getName)
                .toList();
    }

    public static List<String> getCleanEnergyNames() {
        return Arrays.stream(values())
                .filter(EnergySource::isCleanEnergy)
                .map(EnergySource::getName)
                .toList();
    }

    public static boolean isCleanEnergySource(String sourceName) {
        return Arrays.stream(values())
                .anyMatch(source -> source.getName().equals(sourceName) && source.isCleanEnergy());
    }
}