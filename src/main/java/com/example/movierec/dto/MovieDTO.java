package com.example.movierec.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Integer movieId;
    private String title;
    private String description;
    private String director;
    private LocalDate releaseDate;
    private Integer duration;
    private String posterUrl;
    private BigDecimal avgRating;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private List<String> actors;
    private List<String> genres;
    private Boolean isLiked; // 用户是否收藏
    private Integer shareCount; // 分享次数
    private Integer favoriteCount; // 收藏次数（favorites 在前端中对应收藏数）
}