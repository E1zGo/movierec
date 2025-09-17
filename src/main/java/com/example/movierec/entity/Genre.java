package com.example.movierec.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer genreId;

    @Column(nullable = false, unique = true)
    private String genreName;
}
