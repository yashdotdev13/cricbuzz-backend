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

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final CricketApiClient cricketApiClient;
    private final MatchService matchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Scheduled ingestion every 15 seconds.
     * Polls CricAPI for live matches and updates scores.
     */
    @Scheduled(fixedRate = 15000)
    public void ingestLiveData() {
        log.info("🚀 Starting ingestion cycle...");

        try {
            String jsonResponse = cricketApiClient.getLiveScores();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode dataNode = rootNode.path("data");
            if (!dataNode.isArray() || dataNode.isEmpty()) {
                log.warn("⚠️ No live matches found in response.");
                return;
            }

            for (JsonNode matchNode : dataNode) {
                String matchId = matchNode.path("id").asText();
                String name = matchNode.path("name").asText("Unknown Match");
                String status = matchNode.path("status").asText("Unknown");
                String team1 = matchNode.path("t1").asText();
                String team2 = matchNode.path("t2").asText();

                if (!status.equalsIgnoreCase("live")) continue;

                log.info("📊 Processing Live Match: {} ({}) - Status: {}", name, matchId, status);

                // Parse Score Data
                JsonNode team1ScoreNode = matchNode.path("t1s");
                JsonNode team2ScoreNode = matchNode.path("t2s");

                String team1Score = team1ScoreNode.asText("");
                String team2Score = team2ScoreNode.asText("");

                // Determine which team is batting based on non-empty score
                String battingTeam = !team1Score.isEmpty() ? team1 : team2;
                String scoreString = !team1Score.isEmpty() ? team1Score : team2Score;

                // Extract numeric data
                int runs = extractRuns(scoreString);
                int wickets = extractWickets(scoreString);
                double overs = extractOvers(scoreString);

                // Map to DTO
                ScoreSnapshotDto snapshot = ScoreSnapshotDto.builder()
                        .id(Long.valueOf(matchId.hashCode())) // generate numeric ID
                        .battingTeam(battingTeam)
                        .runs(runs)
                        .wickets(wickets)
                        .overs(overs)
                        .lastBall("Live update not available in free API")
                        .build();

                matchService.updateMatchScore(snapshot);

                // Commentary placeholder
                CommentaryEventDto commentary = CommentaryEventDto.builder()
                        .id(snapshot.getId())
                        .over(String.valueOf(snapshot.getOvers()))
                        .text(String.format("%s: %s/%s (%.1f ov)", battingTeam, runs, wickets, overs))
                        .build();

                matchService.addCommentaryEvent(commentary);
            }

            log.info("✅ Ingestion cycle completed successfully.");

        } catch (Exception e) {
            log.error("❌ Error during ingestion cycle: {}", e.getMessage(), e);
        }
    }

    // ---------- Helper Methods ---------- //

    private int extractRuns(String scoreString) {
        try {
            String[] parts = scoreString.split("/");
            return Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private int extractWickets(String scoreString) {
        try {
            String[] parts = scoreString.split("/");
            if (parts.length > 1) {
                String w = parts[1].split("\\(")[0].replaceAll("[^0-9]", "");
                return Integer.parseInt(w);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double extractOvers(String scoreString) {
        try {
            int start = scoreString.indexOf('(');
            int end = scoreString.indexOf(')');
            if (start != -1 && end != -1) {
                String oversPart = scoreString.substring(start + 1, end).replace("ov", "").trim();
                return Double.parseDouble(oversPart);
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
