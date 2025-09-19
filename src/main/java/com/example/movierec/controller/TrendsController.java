package com.example.movierec.controller;

import com.example.movierec.dto.ApiResponse;
import com.example.movierec.dto.WordCloudDTO;
import com.example.movierec.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trends")
@CrossOrigin(origins = "http://localhost:8081")
public class TrendsController {

    @Autowired
    private SearchService searchService;

    /**
     * 获取词云数据
     */
    @GetMapping("/word-cloud")
    public ApiResponse<List<WordCloudDTO>> getWordCloudData() {
        List<WordCloudDTO> wordCloud = searchService.getWordCloudData();
        return ApiResponse.success(wordCloud);
    }
}