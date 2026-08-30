package com.company.cricbuzz_backend.entity;




import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreSnapshot {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private String battingTeam;

    private int runs;
    private int wickets;

    private double overs;

    private String lastBall;

    private LocalDateTime snapshotTime;
}
