    // src/main/java/com/example/movierec/controller/ReportController.java
    package com.example.movierec.controller;

    import com.example.movierec.dto.ApiResponse;
    import com.example.movierec.dto.ReportRequestDto;
    import com.example.movierec.dto.ReportResponseDto;
    import com.example.movierec.service.ReportService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/reports")
    @CrossOrigin(origins = "http://localhost:8081")
    public class ReportController {

        @Autowired
        private ReportService reportService;

        /**
         * 提交举报
         * @param reportRequest 举报请求
         * @return 举报结果
         */
        @PostMapping
        public ApiResponse<ReportResponseDto> submitReport(@RequestBody ReportRequestDto reportRequest) {
            try {
                ReportResponseDto result = reportService.submitReport(reportRequest);
                // 修正参数顺序：message在前，data在后
                return ApiResponse.success("举报提交成功", result);
            } catch (Exception e) {
                return ApiResponse.error("举报提交失败: " + e.getMessage());
            }
        }

        /**
         * 获取用户的举报记录
         * @param userId 用户ID
         * @return 举报记录列表
         */
        @GetMapping("/user/{userId}")
        public ApiResponse<List<ReportResponseDto>> getUserReports(@PathVariable Integer userId) {
            try {
                List<ReportResponseDto> reports = reportService.getUserReports(userId);
                return ApiResponse.success(reports);
            } catch (Exception e) {
                return ApiResponse.error("获取举报记录失败: " + e.getMessage());
            }
        }
    }