package com.example.backend.client;

import com.example.backend.dto.CarbonIntensityResponseDto;
import com.example.backend.dto.GenerationIntervalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CarbonIntensityClient {

    private final RestTemplate restTemplate;

    @Value("${carbon.intensity.api-url}")
    private String apiUrl;

    public List<GenerationIntervalDto> getGenerationData(ZonedDateTime start, ZonedDateTime end) {
        String from = formatForApi(start);
        String to = formatForApi(end);

        String url = apiUrl + from + "/" + to;

        CarbonIntensityResponseDto response =
                restTemplate.getForObject(url, CarbonIntensityResponseDto.class);

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData();
    }

    private String formatForApi(ZonedDateTime dateTime) {
        return dateTime
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
    }
}