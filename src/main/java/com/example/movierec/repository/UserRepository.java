package com.example.movierec.repository;

import com.example.movierec.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * 获取用户的搜索历史（这里简化为从评论中提取）
     */
    @Query("SELECT DISTINCT m.title FROM UserMovieRating r " +
            "JOIN r.movie m WHERE r.user.userId = :userId " +
            "ORDER BY r.createdAt DESC")
    List<String> findUserSearchHistory(@Param("userId") Integer userId);
}