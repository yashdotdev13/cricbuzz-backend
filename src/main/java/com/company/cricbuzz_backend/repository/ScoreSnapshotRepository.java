package com.company.cricbuzz_backend.repository;

import com.company.cricbuzz_backend.entity.Match;
import com.company.cricbuzz_backend.entity.ScoreSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreSnapshotRepository extends JpaRepository<ScoreSnapshot, Long> {


    List<ScoreSnapshot> findByMatchOrderBySnapshotTimeAsc(Match match);
}
