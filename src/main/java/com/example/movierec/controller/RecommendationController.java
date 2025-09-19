package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.dto.MovieDTO;
import com.example.movierec.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:8081")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取用户推荐电影
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<MovieDTO>> getUserRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "25") int limit) {
        List<MovieDTO> recommendations = recommendationService.getUserRecommendations(userId, limit);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取最新推荐
     */
    @GetMapping("/latest/{userId}")
    public ApiResponse<List<MovieDTO>> getLatestRecommendations(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<MovieDTO> recommendations = recommendationService.getLatestRecommendations(userId, limit);
        return ApiResponse.success(recommendations);
    }
}