package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.dto.WordCloudDTO;
import com.example.movierec.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:8081")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /** 热门搜索词 */
    @GetMapping("/hot-words")
    public ApiResponse<List<String>> getHotSearchWords() {
        return ApiResponse.success(searchService.getHotSearchWords());
    }

    /** 用户搜索历史 */
    @GetMapping("/history/{userId}")
    public ApiResponse<List<String>> getUserSearchHistory(@PathVariable Integer userId) {
        return ApiResponse.success(searchService.getUserSearchHistory(userId));
    }

    /** 保存搜索历史 */
    @PostMapping("/history/{userId}")
    public ApiResponse<Void> saveSearchHistory(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        searchService.saveSearchHistory(userId, request.get("query"));
        return ApiResponse.success(null);
    }

    /** 清空用户搜索历史 */
    @DeleteMapping("/history/{userId}")
    public ApiResponse<Void> clearUserSearchHistory(@PathVariable Integer userId) {
        searchService.clearUserSearchHistory(userId);
        return ApiResponse.success(null);
    }

    /** 分类发现（电影类型列表） */
    @GetMapping("/genres")
    public ApiResponse<List<String>> getAllGenres() {
        return ApiResponse.success(searchService.getAllGenres());
    }

    /** 词云数据 */
    @GetMapping("/word-cloud")
    public ApiResponse<List<WordCloudDTO>> getWordCloudData() {
        return ApiResponse.success(searchService.getWordCloudData());
    }
}

