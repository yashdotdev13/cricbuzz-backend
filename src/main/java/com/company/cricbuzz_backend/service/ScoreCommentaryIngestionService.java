package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.client.CricketApiClient;
import com.company.cricbuzz_backend.entity.CommentaryEvent;
import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.ScoreSnapshot;
import com.company.cricbuzz_backend.repository.CommentaryEventRepository;
import com.company.cricbuzz_backend.repository.MatchRepository;
import com.company.cricbuzz_backend.repository.ScoreSnapshotRepository;
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

    @Scheduled(fixedRate = 60000)
    public void fetchLiveScoreAndCommentary() {

        List<Match> liveMatches = matchRepository.findByStatus("LIVE");

        if (liveMatches.isEmpty()) {
            log.info("No live matches found for score/commentary ingestion");
            return;
        }
        log.info(
                "Starting score/commentary ingestion for {} live match(es)",
                liveMatches.size()
        );

        for (Match match : liveMatches) {
            try {

                log.info(
                        "Fetching score and commentary for match: {} [{}]",
                        match.getTitle(),
                        match.getExternalId()
                );

                fetchAndSaveScore(match);

                fetchAndSaveCommentary(match);

            } catch (Exception e) {

                log.error(
                        "Failed to update score/commentary for match {}: {}",
                        match.getTitle(),
                        e.getMessage(),
                        e
                );
            }
        }
    }

    /**
     * Fetches and stores the latest score snapshot for a match.
     */
    private void fetchAndSaveScore(Match match) throws Exception {

        String scoreJson =
                cricketApiClient.getMatchScore(match.getExternalId());

        if (scoreJson == null || scoreJson.isBlank()) {
            log.warn(
                    "Empty score response received for match: {}",
                    match.getTitle()
            );
            return;
        }

        JsonNode scoreNode = mapper.readTree(scoreJson);

        if (!scoreNode.has("data") || scoreNode.get("data").isMissingNode()) {
            log.warn(
                    "No score data found for match: {}",
                    match.getTitle()
            );
            return;
        }

        JsonNode data = scoreNode.get("data");

        ScoreSnapshot snapshot = new ScoreSnapshot();
        snapshot.setMatch(match);
        snapshot.setRuns(data.path("score").asInt(0));
        snapshot.setWickets(data.path("wickets").asInt(0));

        snapshot.setBattingTeam(
                data.path("battingTeam").asText(null)
        );

        snapshot.setLastBall(
                data.path("lastBall").asText(null)
        );

        snapshot.setSnapshotTime(LocalDateTime.now());
        scoreSnapshotRepository.save(snapshot);
        log.info(
                "Saved score snapshot for match: {}",
                match.getTitle()
        );
    }

    private void fetchAndSaveCommentary(Match match) throws Exception {

        String commentaryJson =
                cricketApiClient.getCommentary(match.getExternalId());

        if (commentaryJson == null || commentaryJson.isBlank()) {
            log.warn(
                    "Empty commentary response received for match: {}",
                    match.getTitle()
            );
            return;
        }

        JsonNode commentaryNode = mapper.readTree(commentaryJson);

        if (!commentaryNode.has("data")
                || !commentaryNode.get("data").isArray()) {

            log.warn(
                    "No commentary data found for match: {}",
                    match.getTitle()
            );
            return;
        }

        JsonNode commentaryData = commentaryNode.get("data");

        if (commentaryData.isEmpty()) {
            log.info(
                    "No commentary events available for match: {}",
                    match.getTitle()
            );
            return;
        }

        int savedEvents = 0;

        for (JsonNode eventNode : commentaryData) {

            CommentaryEvent event = new CommentaryEvent();

            event.setMatch(match);

            event.setOver(
                    eventNode.path("over").asText(null)
            );

            event.setBatsman(
                    eventNode.path("batsman").asText(null)
            );

            event.setBowler(
                    eventNode.path("bowler").asText(null)
            );

            event.setText(
                    eventNode.path("text").asText(null)
            );

            event.setEventTime(LocalDateTime.now());

            commentaryEventRepository.save(event);

            savedEvents++;
        }

        log.info(
                "Saved {} commentary event(s) for match: {}",
                savedEvents,
                match.getTitle()
        );
    }
}