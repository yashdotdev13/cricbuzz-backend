package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.client.CricketApiClient;
import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.Team;
import com.company.cricbuzz_backend.repository.CommentaryEventRepository;
import com.company.cricbuzz_backend.repository.MatchRepository;
import com.company.cricbuzz_backend.repository.TeamRepository;
import com.company.cricbuzz_backend.service.Impl.MatchServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class IngestionService {

    private final CricketApiClient cricketApiClient;
    private final MatchRepository matchRepository;
    private final CommentaryEventRepository commentaryEventRepository;
    private final TeamRepository teamRepository;
    private final MatchServiceImpl matchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 1500000)
    @Synchronized
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void ingestLiveData() {
        log.info("🚀 Starting ingestion cycle...");
        try {
            String jsonResponse = cricketApiClient.getLiveScores();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode dataNode = rootNode.path("data");

            if (!dataNode.isArray() || dataNode.isEmpty()) {
                log.warn("⚠️ No matches found in API response.");
                return;
            }

            for (JsonNode matchNode : dataNode) {
                processMatchNode(matchNode);
            }

            log.info("✅ Ingestion cycle completed successfully.");

        } catch (Exception e) {
            log.error("❌ Error during ingestion cycle: {}", e.getMessage(), e);
        }
    }

    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200)
    )
    @Transactional
    public void processMatchNode(JsonNode matchNode) {
        String matchIdStr = matchNode.path("id").asText();
        Long matchId = (long) matchIdStr.hashCode(); // numeric ID
        String title = matchNode.path("name").asText("Unknown Match");
        String team1Name = matchNode.path("t1").asText("Team 1");
        String team2Name = matchNode.path("t2").asText("Team 2");
        String status = matchNode.path("status").asText("Scheduled");
        LocalDateTime startTime = LocalDateTime.now();

        // --- Create or fetch Teams ---
        Team team1 = teamRepository.findByName(team1Name)
                .orElseGet(() -> teamRepository.save(Team.builder().name(team1Name).build()));
        Team team2 = teamRepository.findByName(team2Name)
                .orElseGet(() -> teamRepository.save(Team.builder().name(team2Name).build()));

        // --- Create or update Match ---
        Match match = matchRepository.findById(matchId)
                .orElse(Match.builder().id(matchId).build());

        match.setTitle(title);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStatus(status);
        match.setStartTime(startTime);

        // Save match with optimistic locking handling
        matchRepository.save(match);

        // --- Score Parsing if available ---
        String team1Score = matchNode.path("t1s").asText("");
        String team2Score = matchNode.path("t2s").asText("");
        if (!team1Score.isEmpty() || !team2Score.isEmpty()) {
            String battingTeamName = !team1Score.isEmpty() ? team1Name : team2Name;
            String scoreString = !team1Score.isEmpty() ? team1Score : team2Score;

            int runs = extractRuns(scoreString);
            int wickets = extractWickets(scoreString);
            double overs = extractOvers(scoreString);

            ScoreSnapshotDto snapshotDto = ScoreSnapshotDto.builder()
                    .id(match.getId())
                    .battingTeam(battingTeamName)
                    .runs(runs)
                    .wickets(wickets)
                    .overs(overs)
                    .lastBall("Live update not available in free API")
                    .build();

            matchService.updateMatchScore(snapshotDto);

            CommentaryEventDto commentary = CommentaryEventDto.builder()
                    .id(match.getId())
                    .over(String.valueOf(overs))
                    .text(String.format("%s: %s/%s (%.1f ov)", battingTeamName, runs, wickets, overs))
                    .build();

            matchService.addCommentaryEvent(commentary);
        }
    }

    // ---------- Helper Methods ----------
    private int extractRuns(String scoreString) {
        try {
            String[] parts = scoreString.split("/");
            return Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
        } catch (Exception e) { return 0; }
    }

    private int extractWickets(String scoreString) {
        try {
            String[] parts = scoreString.split("/");
            if (parts.length > 1) return Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
            return 0;
        } catch (Exception e) { return 0; }
    }

    private double extractOvers(String scoreString) {
        try {
            int start = scoreString.indexOf('(');
            int end = scoreString.indexOf(')');
            if (start != -1 && end != -1)
                return Double.parseDouble(scoreString.substring(start + 1, end).replace("ov", "").trim());
            return 0.0;
        } catch (Exception e) { return 0.0; }
    }
}
