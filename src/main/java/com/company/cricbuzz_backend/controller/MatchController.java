package com.company.cricbuzz_backend.controller;

import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.MatchDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import com.company.cricbuzz_backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;

    // ---------------- Match Endpoints ---------------- //

    @GetMapping
    public ResponseEntity<List<MatchDto>> getAllMatches() {
        log.info("Fetching all matches");
        List<MatchDto> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/live")
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        log.info("Fetching live matches");
        List<MatchDto> liveMatches = matchService.getLiveMatches();
        return ResponseEntity.ok(liveMatches);
    }

    @GetMapping("/team/{teamName}")
    public ResponseEntity<List<MatchDto>> getMatchesByTeam(@PathVariable String teamName) {
        log.info("Fetching matches for team: {}", teamName);
        List<MatchDto> matches = matchService.getMatchesByTeam(teamName);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDto> getMatchById(@PathVariable Long matchId) {
        log.info("Fetching match by ID: {}", matchId);
        MatchDto match = matchService.getMatchById(matchId);
        return ResponseEntity.ok(match);
    }

    // ---------------- Score Endpoints ---------------- //

    @GetMapping("/{matchId}/score")
    public ResponseEntity<ScoreSnapshotDto> getLatestScore(@PathVariable Long matchId) {
        log.info("Fetching latest score for match ID: {}", matchId);
        ScoreSnapshotDto score = matchService.getLatestScore(matchId);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/{matchId}/score/history")
    public ResponseEntity<List<ScoreSnapshotDto>> getScoreHistory(@PathVariable Long matchId) {
        log.info("Fetching score history for match ID: {}", matchId);
        List<ScoreSnapshotDto> history = matchService.getScoreHistory(matchId);
        return ResponseEntity.ok(history);
    }

    // ---------------- Commentary Endpoints ---------------- //

    @GetMapping("/{matchId}/commentary")
    public ResponseEntity<List<CommentaryEventDto>> getCommentaryByMatch(@PathVariable Long matchId) {
        log.info("Fetching commentary for match ID: {}", matchId);
        List<CommentaryEventDto> commentaryList = matchService.getCommentaryByMatch(matchId);
        return ResponseEntity.ok(commentaryList);
    }

    // ---------------- Admin / Update Endpoints ---------------- //
    // These are optional if you want to allow manual updates via API
    @PostMapping("/{matchId}/score")
    public ResponseEntity<Void> updateMatchScore(@PathVariable Long matchId,
                                                 @RequestBody ScoreSnapshotDto scoreSnapshotDto) {
        log.info("Updating match score for match ID: {}", matchId);
        scoreSnapshotDto.setId(matchId);
        matchService.updateMatchScore(scoreSnapshotDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/commentary")
    public ResponseEntity<Void> addCommentaryEvent(@PathVariable Long matchId,
                                                   @RequestBody CommentaryEventDto commentaryEventDto) {
        log.info("Adding commentary for match ID: {}", matchId);
        commentaryEventDto.setId(matchId);
        matchService.addCommentaryEvent(commentaryEventDto);
        return ResponseEntity.ok().build();
    }
}
