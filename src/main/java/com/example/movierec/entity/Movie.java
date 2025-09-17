package com.example.movierec.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String director;
    private LocalDate releaseDate;
    private Integer duration;
    private String posterUrl;

    private Double avgRating = 0.0;
    private Integer ratingCount = 0;
    private LocalDateTime createdAt;
}
