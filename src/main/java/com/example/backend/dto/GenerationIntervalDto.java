package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenerationIntervalDto {

    private String from;
    private String to;
    private List<GenerationMixDto> generationmix;
}