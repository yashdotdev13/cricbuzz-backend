package com.company.cricbuzz_backend.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commentary_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private String over;

    private String batsman;

    private String bowler;

    private String text;

    private LocalDateTime eventTime;
}
