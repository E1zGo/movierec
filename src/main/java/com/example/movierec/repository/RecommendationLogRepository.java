package com.example.movierec.repository;

import com.example.movierec.entity.RecommendationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Integer> {

    /**
     * 获取用户的推荐记录
     */
    @Query("SELECT r FROM RecommendationLog r WHERE r.user.userId = :userId " +
            "ORDER BY r.recommendationScore DESC, r.recommendedAt DESC")
    List<RecommendationLog> findByUserIdOrderByScoreDesc(@Param("userId") Integer userId, Pageable pageable);

    /**
     * 获取用户最新的推荐记录
     */
    @Query("SELECT r FROM RecommendationLog r WHERE r.user.userId = :userId " +
            "ORDER BY r.recommendedAt DESC")
    List<RecommendationLog> findLatestRecommendations(@Param("userId") Integer userId, Pageable pageable);
}