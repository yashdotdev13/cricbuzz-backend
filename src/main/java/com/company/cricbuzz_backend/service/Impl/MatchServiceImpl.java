package com.company.cricbuzz_backend.service.Impl;


import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.MatchDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.exceptions.ResourceNotFoundException;
import com.company.cricbuzz_backend.repository.*;
import com.company.cricbuzz_backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final ScoreSnapshotRepository scoreSnapshotRepository;
    private final CommentaryEventRepository commentaryEventRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // ======== Match methods    ======== //

    @Override
    public MatchDto getMatchById(Long matchId) {
        log.info("Fetching match by ID: {}", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(()->new ResourceNotFoundException("match not found not found with id:"+matchId));
        return modelMapper.map(match, MatchDto.class);
    }

    @Override
    public List<MatchDto> getAllMatches() {
        log.info("Fetching all the matches");
        return matchRepository.findAll()
                .stream()
                .map(match-> modelMapper.map(match, MatchDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDto> getLiveMatches() {
        log.info("Fetching live matches");
        return matchRepository.findByStatus("Live")
                .stream()
                .map(match -> modelMapper.map(match, MatchDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDto> getMatchesByTeam(String teamName) {
        return List.of();
    }

    @Override
    public ScoreSnapshotDto getLatestScore(Long matchId) {
        return null;
    }

    @Override
    public List<ScoreSnapshotDto> getScoreHistory(Long matchId) {
        return List.of();
    }

    @Override
    public List<CommentaryEventDto> getCommentaryByMatch(Long matchId) {
        return List.of();
    }

    @Override
    public void updateMatchScore(ScoreSnapshotDto scoreSnapshotDto) {

    }

    @Override
    public void addCommentaryEvent(CommentaryEventDto commentaryEventDto) {

    }
}
