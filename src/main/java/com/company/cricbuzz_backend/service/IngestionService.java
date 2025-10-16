package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.client.CricketApiClient;
import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final CricketApiClient cricketApiClient;
    private final MatchService matchService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Scheduled(fixedRate = 15000)
    public void ingestLiveData() {
        log.info("Starting ingestion cycle...");

        try {
            String jsonResponse = cricketApiClient.getLiveMatches();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode matches = rootNode.path("matches");

            if (!matches.isArray()) {
                log.warn("No matches found in response.");
                return;
            }

            Iterator<JsonNode> iterator = matches.elements();
            while (iterator.hasNext()) {
                JsonNode matchNode = iterator.next();

                Long matchId = matchNode.path("id").asLong();
                String status = matchNode.path("status").asText();

                if (!"Live".equalsIgnoreCase(status)) continue;

                // Map to ScoreSnapshotDto
                ScoreSnapshotDto snapshot = new ScoreSnapshotDto();
                snapshot.setId(matchId);
                snapshot.setBattingTeam(matchNode.path("batting_team").asText());
                snapshot.setRuns(matchNode.path("teamScore").asInt());
                snapshot.setWickets(matchNode.path("wickets").asInt());
                snapshot.setOvers(matchNode.path("overs").asDouble());
                snapshot.setLastBall(matchNode.path("last_ball").asText("No commentary yet"));


                // Update match score
                matchService.updateMatchScore(snapshot);

                // Create sample commentary event
                CommentaryEventDto commentary = new CommentaryEventDto();
                commentary.setId(matchId);
                commentary.setOver(String.valueOf(snapshot.getOvers()));
                commentary.setText(matchNode.path("lastBall").asText("No commentary yet."));

                matchService.addCommentaryEvent(commentary);
            }

            log.info("✅ Ingestion cycle completed successfully.");

        } catch (Exception e) {
            log.error("Error during ingestion cycle: {}", e.getMessage(), e);
        }
    }
}