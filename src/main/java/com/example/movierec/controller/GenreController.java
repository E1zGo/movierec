package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.entity.Genre;
import com.example.movierec.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "http://localhost:8081")
public class GenreController {

    @Autowired
    private GenreService genreService;

    /**
     * 获取所有电影类型
     */
    @GetMapping
    public ApiResponse<List<Genre>> getAllGenres() {
        // 修复: 调用 service 层获取并返回所有类型
        return ApiResponse.success(genreService.getAllGenres());
    }

    /**
     * 获取热门类型
     */
    @GetMapping("/hot")
    public ApiResponse<List<Genre>> getHotGenres() {
        List<Genre> hotGenres = genreService.getHotGenres();
        return ApiResponse.success(hotGenres);
    }
}