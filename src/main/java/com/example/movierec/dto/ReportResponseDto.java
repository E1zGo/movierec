package com.example.movierec.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportResponseDto {
    private Integer id;
    private String reporterName;
    private String reportedUserName;
    private String reportedComment;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}