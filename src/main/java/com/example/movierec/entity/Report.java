package com.example.movierec.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reporter_id", nullable = false)
    private Integer reporterId;

    @Column(name = "reported_user_id", nullable = false)
    private Integer reportedUserId;

    @Column(name = "reporter_name", nullable = false)
    private String reporterName;

    @Column(name = "reported_user_name", nullable = false)
    private String reportedUserName;

    @Column(name = "reported_comment", columnDefinition = "TEXT")
    private String reportedComment;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public enum ReportStatus {
        PENDING,    // 待处理
        APPROVED,   // 已通过
        REJECTED    // 已拒绝
    }
}
