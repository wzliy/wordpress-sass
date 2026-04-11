package com.wpss.wordpresssass.report.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.report.application.AdminReportApplicationService;
import com.wpss.wordpresssass.report.application.dto.AdminOrderReportDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportApplicationService adminReportApplicationService;

    public AdminReportController(AdminReportApplicationService adminReportApplicationService) {
        this.adminReportApplicationService = adminReportApplicationService;
    }

    @GetMapping
    public ApiResponse<AdminOrderReportDto> overview(@RequestParam(value = "siteId", required = false) Long siteId,
                                                     @RequestParam(value = "dateFrom", required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                     @RequestParam(value = "dateTo", required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ApiResponse.success(adminReportApplicationService.overview(siteId, dateFrom, dateTo));
    }
}
