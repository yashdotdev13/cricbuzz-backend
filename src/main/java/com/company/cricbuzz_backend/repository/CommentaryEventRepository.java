package com.company.cricbuzz_backend.repository;

import com.company.cricbuzz_backend.entity.CommentaryEvent;
import com.company.cricbuzz_backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentaryEventRepository extends JpaRepository<CommentaryEvent, Long> {
    List<CommentaryEvent> findByMatchOrderByEventTimeAsc(Match match);
}

