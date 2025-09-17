package com.example.movierec.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String phoneNumber;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, BANNED, ADMIN

    private Integer age;
    private String gender;

    private LocalDateTime registrationDate;
}
