package com.example.backend.dto;

public class ApiErrorDto {

    private String message;

    public ApiErrorDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}