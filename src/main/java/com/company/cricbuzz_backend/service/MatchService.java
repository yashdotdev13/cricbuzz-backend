package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.MatchDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;

import java.util.List;

public interface MatchService {

    MatchDto getMatchById(Long matchId);
    List<MatchDto> getAllMatches();
    List<MatchDto> getLiveMatches();
    List<MatchDto> getMatchesByTeam(String teamName);

    ScoreSnapshotDto getLatestScore(Long matchId);
    List<ScoreSnapshotDto> getScoreHistory(Long matchId);


    List<CommentaryEventDto> getCommentaryByMatch(Long matchId);

    void updateMatchScore(Long matchId, ScoreSnapshotDto scoreSnapshotDto);

    void addCommentaryEvent(Long matchId, CommentaryEventDto commentaryEventDto);
}
