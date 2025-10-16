package com.company.cricbuzz_backend.repository;

import com.company.cricbuzz_backend.entity.Player;
import com.company.cricbuzz_backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player,Long> {

    List<Player> findByTeam(Team team);
}
