package com.example.backend.dto;

import java.util.List;

public class GenerationIntervalDto {

    private String from;
    private String to;
    private List<GenerationMixDto> generationmix;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<GenerationMixDto> getGenerationmix() {
        return generationmix;
    }

    public void setGenerationmix(List<GenerationMixDto> generationmix) {
        this.generationmix = generationmix;
    }
}