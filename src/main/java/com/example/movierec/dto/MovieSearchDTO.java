package com.example.movierec.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovieSearchDTO {
    private Integer movieId;
    private String title;
    private String posterUrl;
    private LocalDate releaseDate;
    private BigDecimal avgRating;
    private String description;
}
