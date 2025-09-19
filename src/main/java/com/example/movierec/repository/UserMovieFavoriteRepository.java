package com.example.movierec.repository;

import com.example.movierec.entity.Movie;
import com.example.movierec.entity.User;
import com.example.movierec.entity.UserMovieFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieFavoriteRepository extends JpaRepository<UserMovieFavorite, Integer> {

    /**
     * 查找特定用户对特定电影的收藏记录
     */
    Optional<UserMovieFavorite> findByUserAndMovie(User user, Movie movie);

    /**
     * 查找特定用户的所有收藏
     */
    List<UserMovieFavorite> findByUser(User user);

    /**
     * 查找特定用户的所有收藏，按时间倒序
     */
    List<UserMovieFavorite> findByUserOrderByAddedAtDesc(User user);

    /**
     * 统计特定电影的收藏数量
     */
    Long countByMovie(Movie movie);

    /**
     * 检查特定用户是否已收藏特定电影
     */
    boolean existsByUserAndMovie(User user, Movie movie);

    /**
     * 查找收藏了特定电影的所有用户
     */
    List<UserMovieFavorite> findByMovie(Movie movie);

    /**
     * 获取用户收藏的电影ID列表
     */
    @Query("SELECT f.movie.movieId FROM UserMovieFavorite f WHERE f.user.userId = :userId")
    List<Integer> findMovieIdsByUserId(@Param("userId") Integer userId);
}