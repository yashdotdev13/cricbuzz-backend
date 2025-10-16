package com.company.cricbuzz_backend.dtos;




import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaryEventDto {

    private Long id;
    private String over;
    private String batsman;
    private String bowler;
    private String text;
}
