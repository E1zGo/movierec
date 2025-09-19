package com.example.movierec.dto;

import lombok.Data;

@Data
public class ReportRequestDto {
    private Integer reporterId;           // 举报者ID
    private Integer reportedUserId;       // 被举报用户ID
    private String reportedComment;       // 被举报的评论内容
    private String reason;                // 举报原因
}

