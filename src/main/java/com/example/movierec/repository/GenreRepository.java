package com.example.movierec.repository;

import com.example.movierec.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    /**
     * 获取热门类型（按电影数量排序）
     */
    @Query("SELECT g, COUNT(m) as movieCount FROM Genre g " +
            "LEFT JOIN g.movies m " +
            "GROUP BY g " +
            "ORDER BY movieCount DESC")
    List<Object[]> findHotGenres();
}