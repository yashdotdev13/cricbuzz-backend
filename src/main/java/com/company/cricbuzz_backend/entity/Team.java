package com.company.cricbuzz_backend.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String shortName; // e.g., IND, AUS

    private String logoUrl;
}