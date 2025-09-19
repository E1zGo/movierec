package com.example.movierec.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "search_history", columnDefinition = "JSON")
    private String searchHistory; // 新增字段

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private Integer age;

    private String gender;

    @Column(name = "avatar_url")
    private String avatarUrl; // 新增头像字段

    @Column(name = "birthday")
    private String birthday;  // 新增生日字段

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMovieRating> ratings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMovieFavorite> favorites;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
    }

    public enum UserStatus {
        ACTIVE, BANNED, ADMIN
    }
}