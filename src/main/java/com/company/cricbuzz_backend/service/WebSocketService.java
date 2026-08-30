package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastScore(
            Long matchId,
            ScoreSnapshotDto scoreSnapshotDto) {

        try {
            String destination =
                    "/topic/match/" + matchId + "/score";

            log.info("Broadcasting score update to {}", destination);

            messagingTemplate.convertAndSend(
                    destination,
                    scoreSnapshotDto
            );
        }
        catch (Exception e) {
            log.error(
                    "Error broadcasting score for match {}: {}",
                    matchId,
                    e.getMessage(),
                    e
            );
        }
    }

    public void broadcastCommentary(
            Long matchId,
            CommentaryEventDto commentaryEventDto) {

        try {
            String destination =
                    "/topic/match/" + matchId + "/commentary";

            log.info("Broadcasting commentary update to {}", destination);

            messagingTemplate.convertAndSend(
                    destination,
                    commentaryEventDto
            );
        }
        catch (Exception e) {
            log.error(
                    "Error broadcasting commentary for match {}: {}",
                    matchId,
                    e.getMessage(),
                    e
            );
        }
    }
}