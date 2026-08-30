package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.client.CricketApiClient;
import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.ScoreSnapshot;
import com.company.cricbuzz_backend.entity.CommentaryEvent;
import com.company.cricbuzz_backend.repository.MatchRepository;
import com.company.cricbuzz_backend.repository.ScoreSnapshotRepository;
import com.company.cricbuzz_backend.repository.CommentaryEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreCommentaryIngestionService {

    private final CricketApiClient cricketApiClient;
    private final MatchRepository matchRepository;
    private final ScoreSnapshotRepository scoreSnapshotRepository;
    private final CommentaryEventRepository commentaryEventRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(fixedRate = 60000) // every 1 min
    public void fetchLiveScoreAndCommentary() {
        List<Match> liveMatches = matchRepository.findAll()
                .stream().filter(m -> "Live".equalsIgnoreCase(m.getStatus())).toList();

        for (Match match : liveMatches) {
            try {
                // ---- Fetch Score ----
                String scoreJson = cricketApiClient.getMatchScore(match.getExternalId());
                JsonNode scoreNode = mapper.readTree(scoreJson);
                if (scoreNode.has("data")) {
                    JsonNode data = scoreNode.get("data");
                    ScoreSnapshot snapshot = new ScoreSnapshot();
                    snapshot.setMatch(match);
                    snapshot.setRuns(data.path("score").asInt(0));
                    snapshot.setWickets(data.path("wickets").asInt(0));
                    snapshot.setSnapshotTime(LocalDateTime.now());
                    scoreSnapshotRepository.save(snapshot);
                    log.info("Saved score snapshot for match: {}", match.getTitle());
                }

                // ---- Fetch Commentary ----
                String commJson = cricketApiClient.getCommentary(match.getExternalId());
                JsonNode commNode = mapper.readTree(commJson);
                if (commNode.has("data")) {
                    for (JsonNode eventNode : commNode.get("data")) {
                        CommentaryEvent event = new CommentaryEvent();
                        event.setMatch(match);
                        event.setText(eventNode.path("text").asText());
                        event.setEventTime(LocalDateTime.now());
                        commentaryEventRepository.save(event);
                    }
                    log.info("Saved commentary for match: {}", match.getTitle());
                }
            } catch (Exception e) {
                log.error("Failed to update score/commentary for match {}: {}", match.getTitle(), e.getMessage());
            }
        }
    }
}