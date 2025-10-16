package com.company.cricbuzz_backend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String role; // Batsman, Bowler, All-Rounder, WK

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
