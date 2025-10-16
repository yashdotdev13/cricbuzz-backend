package com.company.cricbuzz_backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CricketApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${cricket.api.base-url}")
    private String baseUrl;

    @Value("${cricket.api.key}")
    private String apiKey;

    /**
     * Fetches live cricket scores using the /cricScore endpoint.
     */
    public String getLiveScores() {
        String url = String.format("%s/cricScore?apikey=%s", baseUrl, apiKey);
        log.info("Fetching live scores from CricAPI: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("Received response from CricAPI: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error while fetching live scores: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch live scores from CricAPI", e);
        }
    }

    /**
     * (Optional) Fetch detailed info about a specific match by ID — if available in your plan.
     */
    public String getMatchDetails(String matchId) {
        String url = String.format("%s/match_info?apikey=%s&id=%s", baseUrl, apiKey, matchId);
        log.info("Fetching detailed info for match ID: {}", matchId);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("Received match details: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error while fetching match details for ID {}: {}", matchId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch match details from CricAPI", e);
        }
    }
}
