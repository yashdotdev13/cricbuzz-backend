package com.company.cricbuzz_backend.dtos;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreSnapshotDto {

    private Long id;
    private String battingTeam;
    private int runs;
    private int wickets;

    private Double overs;

    private String lastBall;
}
