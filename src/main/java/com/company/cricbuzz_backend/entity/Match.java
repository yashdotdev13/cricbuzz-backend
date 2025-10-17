package com.company.cricbuzz_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_matches_external_id", columnList = "external_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // External ID from CricAPI (stable string id) - use this to find matches
    @Column(name = "external_id", unique = true, nullable = true)
    private String externalId;

    private String title; // e.g., India vs Australia

    @ManyToOne
    @JoinColumn(name = "team1_id")
    private Team team1;

    @ManyToOne
    @JoinColumn(name = "team2_id")
    private Team team2;

    private String venue;

    private LocalDateTime startTime;

    private String status; // Scheduled, Live, Completed

    // optimistic locking version field
    @Version
    private Long version;
}
