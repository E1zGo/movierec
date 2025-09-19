package com.example.movierec.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDto {
    private Integer id;
    private Integer userId;
    private String username;
    private String content;
    private Integer likes;
    private LocalDateTime createdAt;
    private Integer parentId;
    private String movieTitle; // 电影标题
    private Integer movieId;   // 新增：电影ID，用于跳转
    private List<CommentDto> replies = new ArrayList<>();
}