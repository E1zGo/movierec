package com.example.movierec.repository;

import com.example.movierec.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    // 根据举报者ID查询举报记录
    List<Report> findByReporterIdOrderByCreatedAtDesc(Integer reporterId);

    // 检查是否已经举报过同一条评论
    boolean existsByReporterIdAndReportedUserIdAndReportedComment(Integer reporterId, Integer reportedUserId, String reportedComment);
}