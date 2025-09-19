package com.example.movierec.service;

import com.example.movierec.dto.MovieDTO;
import com.example.movierec.dto.MovieSearchDTO;
import com.example.movierec.entity.Movie;
import com.example.movierec.entity.Actor;
import com.example.movierec.entity.Genre;
import com.example.movierec.repository.MovieRepository;
import com.example.movierec.repository.UserMovieFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserMovieFavoriteRepository userMovieFavoriteRepository;

    /**
     * 获取热门电影
     */
    public List<MovieDTO> getHotMovies(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Movie> movies = movieRepository.findHotMovies(pageable);
        return movies.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据ID获取电影详情
     */
    public Optional<MovieDTO> getMovieById(Integer movieId) {
        return movieRepository.findById(movieId).map(this::convertToDTO);
    }

    /**
     * 搜索电影
     */
    public Page<MovieDTO> searchMovies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieRepository.searchMovies(keyword, pageable);
        return movies.map(this::convertToDTO);
    }

    /**
     * 按类型搜索电影
     */
    public Page<MovieDTO> getMoviesByGenre(String genreName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieRepository.findByGenreNameContaining(genreName, pageable);
        return movies.map(this::convertToDTO);
    }

    /**
     * 获取所有电影（分页）
     */
    public Page<MovieDTO> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieRepository.findAll(pageable);
        return movies.map(this::convertToDTO);
    }

    /**
     * 轻量搜索电影（用于搜索结果页面）
     */
    public Page<MovieSearchDTO> searchMoviesLite(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieRepository.searchMovies(keyword, pageable);

        return movies.map(movie -> {
            MovieSearchDTO dto = new MovieSearchDTO();
            dto.setMovieId(movie.getMovieId());
            dto.setTitle(movie.getTitle());
            dto.setPosterUrl(movie.getPosterUrl());
            dto.setReleaseDate(movie.getReleaseDate());
            dto.setAvgRating(movie.getAvgRating());
            dto.setDescription(movie.getDescription());
            return dto;
        });
    }

    /**
     * 增加分享计数
     */
    @Transactional
    public Movie incrementShareCount(Integer movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));
        movie.setShareCount(movie.getShareCount() + 1);
        return movieRepository.save(movie);
    }

    /**
     * 获取相关推荐电影
     */
    public List<MovieDTO> getRelatedMovies(Integer movieId, int limit) {
        Movie currentMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));

        List<Movie> relatedMovies = movieRepository.findRelatedMoviesByGenres(
                currentMovie.getGenres().stream()
                        .map(Genre::getGenreId)
                        .collect(Collectors.toList()),
                movieId,
                PageRequest.of(0, limit)
        );

        return relatedMovies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 新增: 获取电影的分享总数
     */
    public int getShareCount(Integer movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("电影不存在"));
        return movie.getShareCount() != null ? movie.getShareCount() : 0;
    }

    /**
     * 新增: 按类型 + 年代筛选电影
     */

    /**
     * 新增: 按条件筛选电影的服务方法
     * @param genres 逗号分隔的类型字符串, e.g., "科幻,动作"
     * @param year 年代字符串, e.g., "2010s"
     * @param page 页码
     * @param size 每页数量
     * @return 筛选后的电影DTO分页结果
     */
    public Page<MovieDTO> filterMovies(String genres, String year, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<String> genreList = (genres == null || genres.isEmpty())
                ? Collections.emptyList()
                : Arrays.asList(genres.split(","));

        Long genreCount = (long) genreList.size();

        Integer startYear = null;
        Integer endYear = null;
        if (year != null && !year.isEmpty() && year.endsWith("s")) {
            try {
                // 解析 "2010s" 这样的字符串
                startYear = Integer.parseInt(year.substring(0, 4));
                endYear = startYear + 9;
            } catch (NumberFormatException e) {
                // 如果格式错误，则忽略年份筛选
                startYear = null;
                endYear = null;
            }
        }

        Page<Movie> movies = movieRepository.findWithFilters(
                genreList.isEmpty() ? null : genreList, // 如果列表为空，传递null
                genreCount,
                startYear,
                endYear,
                pageable
        );

        return movies.map(this::convertToDTO);
    }
    /**
     * 转换Movie实体为DTO
     */
    public MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDirector(movie.getDirector());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setDuration(movie.getDuration());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setAvgRating(movie.getAvgRating());
        dto.setRatingCount(movie.getRatingCount());
        dto.setCreatedAt(movie.getCreatedAt());

        dto.setShareCount(movie.getShareCount() != null ? movie.getShareCount() : 0);

        if (movie.getActors() != null) {
            dto.setActors(movie.getActors().stream()
                    .map(Actor::getActorName)
                    .collect(Collectors.toList()));
        }

        if (movie.getGenres() != null) {
            dto.setGenres(movie.getGenres().stream()
                    .map(Genre::getGenreName)
                    .collect(Collectors.toList()));
        }

        Long favoriteCount = userMovieFavoriteRepository.countByMovie(movie);
        dto.setFavoriteCount(favoriteCount.intValue());

        return dto;
    }
}
