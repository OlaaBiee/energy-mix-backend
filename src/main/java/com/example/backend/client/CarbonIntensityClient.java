package com.example.backend.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CarbonIntensityClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.carbonintensity.org.uk/generation/";

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getGenerationData(ZonedDateTime start, ZonedDateTime end) {
        String from = formatForApi(start);
        String to = formatForApi(end);

        String url = API_URL + from + "/" + to;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || response.get("data") == null) {
            return Collections.emptyList();
        }

        return (List<Map<String, Object>>) response.get("data");
    }

    private String formatForApi(ZonedDateTime dateTime) {
        return dateTime
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
    }
}