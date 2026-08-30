package com.company.cricbuzz_backend.service;

import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.MatchDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;

import java.util.List;

public interface MatchService {


    //========= Match related =============//
    MatchDto getMatchById(Long matchId);
    List<MatchDto> getAllMatches();
    List<MatchDto> getLiveMatches();
    List<MatchDto> getMatchesByTeam(String teamName);


    //=========score related =============//
    ScoreSnapshotDto getLatestScore(Long matchId);
    List<ScoreSnapshotDto> getScoreHistory(Long matchId);


    //========= commentary related ========//
    List<CommentaryEventDto> getCommentaryByMatch(Long matchId);

    // updated
    void updateMatchScore(Long matchId, ScoreSnapshotDto scoreSnapshotDto);

    void addCommentaryEvent(Long matchId, CommentaryEventDto commentaryEventDto);
}
