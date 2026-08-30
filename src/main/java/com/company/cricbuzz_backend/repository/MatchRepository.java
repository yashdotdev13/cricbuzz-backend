package com.company.cricbuzz_backend.repository;

import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTeam1OrTeam2(Team team1, Team team2);

    List<Match> findByStatus(String status);

    Optional<Match> findByTitleAndTeam1AndTeam2(
            String title,
            Team team1,
            Team team2
    );

    Optional<Match> findByExternalId(String externalId);
}