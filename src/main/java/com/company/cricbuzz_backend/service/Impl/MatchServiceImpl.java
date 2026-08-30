package com.company.cricbuzz_backend.service.Impl;


import com.company.cricbuzz_backend.dtos.CommentaryEventDto;
import com.company.cricbuzz_backend.dtos.MatchDto;
import com.company.cricbuzz_backend.dtos.ScoreSnapshotDto;
import com.company.cricbuzz_backend.entity.CommentaryEvent;
import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.ScoreSnapshot;
import com.company.cricbuzz_backend.entity.Team;
import com.company.cricbuzz_backend.exceptions.ResourceNotFoundException;
import com.company.cricbuzz_backend.repository.*;
import com.company.cricbuzz_backend.service.MatchService;
import com.company.cricbuzz_backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final WebSocketService webSocketService;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public MatchDto getMatchById(Long matchId) {
        log.info("Fetching match by ID: {}", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(()->new ResourceNotFoundException("match not found not found with id:"+matchId));
        return mapToMatchDto(match);
    }

    @Override
    public List<MatchDto> getAllMatches() {
        log.info("Fetching all the matches");
        return matchRepository.findAll()
                .stream()
                .map(this::mapToMatchDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDto> getLiveMatches() {
        log.info("Fetching live matches");

        return matchRepository.findByStatus("LIVE")
                .stream()
                .map(this::mapToMatchDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDto> getMatchesByTeam(String teamName) {
        log.info("Fetching matches for team: {}",teamName);
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(()->new RuntimeException("Team not found: "+teamName));
        return matchRepository.findByTeam1OrTeam2(team, team)
                .stream()
                .map(this::mapToMatchDto)
                .collect(Collectors.toList());
    }

    @Override
    public ScoreSnapshotDto getLatestScore(Long matchId) {
        log.info("Fetching latest score for match ID: {}", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));
        return scoreSnapshotRepository.findByMatchOrderBySnapshotTimeAsc(match)
                .stream()
                .reduce((first, second) -> second) // get the last snapshot
                .map(snapshot -> modelMapper.map(snapshot, ScoreSnapshotDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("No score snapshots available for match ID: " + matchId));
    }

    @Override
    public List<ScoreSnapshotDto> getScoreHistory(Long matchId) {
        log.info("Fetching score history for match ID: {}", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));
        return scoreSnapshotRepository.findByMatchOrderBySnapshotTimeAsc(match)
                .stream()
                .map(snapshot -> modelMapper.map(snapshot, ScoreSnapshotDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentaryEventDto> getCommentaryByMatch(Long matchId) {
        log.info("Fetching commentary for match ID: {}", matchId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));
        return commentaryEventRepository.findByMatchOrderByEventTimeAsc(match)
                .stream()
                .map(event -> modelMapper.map(event, CommentaryEventDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateMatchScore(Long matchId, ScoreSnapshotDto scoreSnapshotDto) {
        log.info("Updating match score for match ID: {}", matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found with ID: " + matchId));

        ScoreSnapshot snapshot = modelMapper.map(scoreSnapshotDto, ScoreSnapshot.class);

        snapshot.setId(null);
        snapshot.setMatch(match);

        snapshot.setSnapshotTime(LocalDateTime.now());

        scoreSnapshotRepository.save(snapshot);

        String redisKey = "match:" + matchId;
        redisTemplate.opsForValue().set(redisKey, snapshot);

        webSocketService.broadcastScore(matchId, scoreSnapshotDto);
    }


    @Override
    public void addCommentaryEvent(
            Long matchId,
            CommentaryEventDto commentaryEventDto) {

        log.info("Adding commentary event for match ID: {}", matchId);

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found with ID: " + matchId));

        CommentaryEvent event =
                modelMapper.map(commentaryEventDto, CommentaryEvent.class);

        event.setId(null);
        event.setMatch(match);

        event.setEventTime(LocalDateTime.now());
        commentaryEventRepository.save(event);

        String redisKey = "commentary:" + matchId;
        redisTemplate.opsForList().rightPush(redisKey, event);

        webSocketService.broadcastCommentary(matchId, commentaryEventDto);
    }


    private MatchDto mapToMatchDto(Match match) {
        return MatchDto.builder()
                .id(match.getId())
                .title(match.getTitle())
                .team1(match.getTeam1() != null ? match.getTeam1().getName() : null)
                .team2(match.getTeam2() != null ? match.getTeam2().getName() : null)
                .venue(match.getVenue())
                .startTime(match.getStartTime())
                .status(match.getStatus())
                .build();
    }
}
