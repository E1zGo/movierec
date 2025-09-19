package com.example.movierec.repository;

import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.entity.UserMovieRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieRatingRepository extends JpaRepository<UserMovieRating, Integer> {

    /**
     * 查找特定用户对特定电影的评分
     */
    Optional<UserMovieRating> findByUserAndMovie(User user, Movie movie);

    /**
     * 查找特定电影的所有评分
     */
    List<UserMovieRating> findByMovie(Movie movie);

    /**
     * 查找特定用户的所有评分
     */
    List<UserMovieRating> findByUser(User user);

    /**
     * 查找特定用户的所有评分，按时间倒序
     */
    List<UserMovieRating> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 统计特定电影的评分数量
     */
    Long countByMovie(Movie movie);

    /**
     * 检查特定用户是否已对特定电影评分
     */
    boolean existsByUserAndMovie(User user, Movie movie);
}