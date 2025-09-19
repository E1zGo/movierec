package com.example.movierec.service;

import com.example.movierec.dto.ReportRequestDto;
import com.example.movierec.dto.ReportResponseDto;
import com.example.movierec.entity.Report;
import com.example.movierec.entity.User;
import com.example.movierec.exception.ResourceNotFoundException;
import com.example.movierec.repository.ReportRepository;
import com.example.movierec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 提交举报
     * @param reportRequest 举报请求
     * @return 举报结果
     */
    @Transactional
    public ReportResponseDto submitReport(ReportRequestDto reportRequest) {
        // 1. 验证举报者和被举报用户是否存在
        User reporter = userRepository.findById(reportRequest.getReporterId())
                .orElseThrow(() -> new ResourceNotFoundException("举报者不存在: " + reportRequest.getReporterId()));

        User reportedUser = userRepository.findById(reportRequest.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("被举报用户不存在: " + reportRequest.getReportedUserId()));

        // 2. 检查是否重复举报同一条评论
        boolean alreadyReported = reportRepository.existsByReporterIdAndReportedUserIdAndReportedComment(
                reportRequest.getReporterId(),
                reportRequest.getReportedUserId(),
                reportRequest.getReportedComment()
        );

        if (alreadyReported) {
            throw new RuntimeException("您已经举报过这条评论了");
        }

        // 3. 创建举报记录
        Report report = new Report();
        report.setReporterId(reportRequest.getReporterId());
        report.setReportedUserId(reportRequest.getReportedUserId());
        report.setReporterName(reporter.getUsername());
        report.setReportedUserName(reportedUser.getUsername());
        report.setReportedComment(reportRequest.getReportedComment());
        report.setReason(reportRequest.getReason());
        report.setStatus(Report.ReportStatus.PENDING);

        // 4. 保存举报记录
        Report savedReport = reportRepository.save(report);

        // 5. 转换为响应DTO并返回
        return convertToResponseDto(savedReport);
    }

    /**
     * 获取用户的举报记录
     */
    public List<ReportResponseDto> getUserReports(Integer userId) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 将Report实体转换为ResponseDto
     */
    private ReportResponseDto convertToResponseDto(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        dto.setReporterName(report.getReporterName());
        dto.setReportedUserName(report.getReportedUserName());
        dto.setReportedComment(report.getReportedComment());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus().name());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}