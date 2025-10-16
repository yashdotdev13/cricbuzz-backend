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
        log.info("Fetching matches for team: {}",teamName);
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(()->new RuntimeException("Team not found: "+teamName));
        return matchRepository.findByTeam1OrTeam2(team, team)
                .stream()
                .map(match-> modelMapper.map(match, MatchDto.class))
                .collect(Collectors.toList());
    }


    //  ========= score methods  ======== //
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
    public void updateMatchScore(ScoreSnapshotDto scoreSnapshotDto) {
        log.info("Updating match score: {}", scoreSnapshotDto);
        try {
            Match match = matchRepository.findById(scoreSnapshotDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + scoreSnapshotDto.getId()));

            ScoreSnapshot snapshot = modelMapper.map(scoreSnapshotDto, ScoreSnapshot.class);
            snapshot.setMatch(match);

            scoreSnapshotRepository.save(snapshot);

            // ---------------- Redis Cache ----------------
            String redisKey = "match:" + match.getId();
            redisTemplate.opsForValue().set(redisKey, snapshot);

            // ---------------- WebSocket Broadcast ----------------
            // TODO: Inject WebSocketService and broadcast snapshot

        } catch (Exception e) {
            log.error("Error updating match score: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to update score for match ID: " + scoreSnapshotDto.getId());
        }
    }


    @Override
    public void addCommentaryEvent(CommentaryEventDto commentaryEventDto) {
        log.info("Adding commentary event: {}", commentaryEventDto);
        try {
            Match match = matchRepository.findById(commentaryEventDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + commentaryEventDto.getId()));

            CommentaryEvent event = modelMapper.map(commentaryEventDto, CommentaryEvent.class);
            event.setMatch(match);

            commentaryEventRepository.save(event);

            // ---------------- Redis Cache ----------------
            String redisKey = "commentary:" + match.getId();
            // For simplicity, can store a List<CommentaryEvent> in Redis
            // TODO: implement adding event to cached list

            // ---------------- WebSocket Broadcast ----------------
            // TODO: Inject WebSocketService and broadcast commentary

        } catch (Exception e) {
            log.error("Error adding commentary event: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to add commentary for match ID: " + commentaryEventDto.getId());
        }
    }
}
