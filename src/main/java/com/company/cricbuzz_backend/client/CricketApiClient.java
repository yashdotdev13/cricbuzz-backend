package com.company.cricbuzz_backend.client;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CricketApiClient {

    private final RestTemplate restTemplate;

    @Value("${cricket.api.base-url}")
    private String baseUrl;

    @Value("${cricket.api.key}")
    private String apiKey;

    public CricketApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public String getLiveMatches() {
        String url = String.format("%s/matches?apikey=%s", baseUrl, apiKey);
        log.info("Fetching live matches from external API: {}", url);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    public String getMatchDetails(String matchId) {
        String url = String.format("%s/match/%s?apikey=%s", baseUrl, matchId, apiKey);
        log.info("Fetching details for match ID: {}", matchId);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }
}
