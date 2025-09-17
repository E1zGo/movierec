package com.example.movierec.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recommendation_logs")
public class RecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "algorithm_type", nullable = false)
    private Integer algorithmType;

    @Column(name = "recommendation_score")
    private Double recommendationScore;

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;

    @PrePersist
    protected void onCreate() {
        recommendedAt = LocalDateTime.now();
    }
}