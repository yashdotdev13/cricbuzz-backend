package com.company.cricbuzz_backend.dtos;

import lombok.*;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {

    private Long id;
    private String title;
    private String team1;
    private String team2;
    private String venue;
    private LocalDateTime startTime;
    private String status;
}
