package com.example.movierec.service;

import com.example.movierec.dto.MovieDTO;
import com.example.movierec.entity.Movie;
import com.example.movierec.entity.RecommendationLog;
import com.example.movierec.repository.RecommendationLogRepository;
import com.example.movierec.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationLogRepository recommendationLogRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    /**
     * 获取用户推荐电影
     */
    public List<MovieDTO> getUserRecommendations(Integer userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<RecommendationLog> logs = recommendationLogRepository.findByUserIdOrderByScoreDesc(userId, pageable);

        if (logs.isEmpty()) {
            // 如果没有推荐记录，返回热门电影
            return movieService.getHotMovies(limit);
        }

        // 核心修改：使用 Stream API 对电影进行去重
        Set<Integer> uniqueMovieIds = new HashSet<>();
        return logs.stream()
                .map(log -> movieService.convertToDTO(log.getMovie()))
                .filter(dto -> uniqueMovieIds.add(dto.getMovieId())) // 添加到 Set，如果返回 true（即不重复），则保留
                .collect(Collectors.toList());
    }

    /**
     * 获取最新推荐
     */
    public List<MovieDTO> getLatestRecommendations(Integer userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<RecommendationLog> logs = recommendationLogRepository.findLatestRecommendations(userId, pageable);

        if (logs.isEmpty()) {
            // 如果没有推荐记录，返回热门电影
            return movieService.getHotMovies(limit);
        }

        // 核心修改：使用 Stream API 对电影进行去重
        Set<Integer> uniqueMovieIds = new HashSet<>();
        return logs.stream()
                .map(log -> movieService.convertToDTO(log.getMovie()))
                .filter(dto -> uniqueMovieIds.add(dto.getMovieId()))
                .collect(Collectors.toList());
    }
}