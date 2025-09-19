package com.example.movierec.repository;

import com.example.movierec.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    @Query("SELECT m.title FROM Movie m ORDER BY m.ratingCount DESC")
    List<String> findTop10ByRatingCount(Pageable pageable);
    /**
     * 根据类型列表和年份范围筛选电影
     */
    /**
     * 新增：根据多个类型和年份范围进行筛选
     * - 如果类型列表 (genres) 为 null 或为空，则忽略类型筛选。
     * - 如果起始年份 (startYear) 为 null，则忽略年份下限。
     * - 如果结束年份 (endYear) 为 null，则忽略年份上限。
     * - HAVING COUNT(g.id) = :genreCount 用于确保电影匹配所有指定的类型。
     */
    @Query("SELECT m FROM Movie m LEFT JOIN m.genres g " + // 更改为 LEFT JOIN
            "WHERE (COALESCE(:genres, NULL) IS NULL OR g.genreName IN :genres) " +
            "AND (cast(:startYear as integer) IS NULL OR YEAR(m.releaseDate) >= :startYear) " +
            "AND (cast(:endYear as integer) IS NULL OR YEAR(m.releaseDate) <= :endYear) " +
            "GROUP BY m.movieId " +
            "HAVING (COALESCE(:genres, NULL) IS NULL OR COUNT(g) = :genreCount)")

    Page<Movie> findWithFilters(
            @Param("genres") List<String> genres,
            @Param("genreCount") Long genreCount,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            Pageable pageable
    );
    /**
     * 获取热门电影（按评分和评论数排序）
     */
    @Query("SELECT m FROM Movie m WHERE m.ratingCount > 0 ORDER BY m.avgRating DESC, m.ratingCount DESC")
    List<Movie> findHotMovies(Pageable pageable);

    /**
     * 按标题搜索电影
     */
    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:keyword%")
    Page<Movie> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按导演搜索电影
     */
    @Query("SELECT m FROM Movie m WHERE m.director LIKE %:director%")
    Page<Movie> findByDirectorContaining(@Param("director") String director, Pageable pageable);

    /**
     * 按类型搜索电影
     */
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g WHERE g.genreName LIKE %:genreName%")
    Page<Movie> findByGenreNameContaining(@Param("genreName") String genreName, Pageable pageable);

    /**
     * 综合搜索电影（标题、导演、类型、演员）
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "LEFT JOIN m.actors a " +
            "WHERE m.title LIKE %:keyword% " +
            "OR m.director LIKE %:keyword% " +
            "OR g.genreName LIKE %:keyword% " +
            "OR a.actorName LIKE %:keyword%")
    Page<Movie> searchMovies(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据类型查找相关电影（排除当前电影）
     */
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g " +
            "WHERE g.genreId IN :genreIds AND m.movieId != :excludeMovieId " +
            "ORDER BY m.avgRating DESC, m.ratingCount DESC")
    List<Movie> findRelatedMoviesByGenres(
            @Param("genreIds") List<Integer> genreIds,
            @Param("excludeMovieId") Integer excludeMovieId,
            Pageable pageable);
}
