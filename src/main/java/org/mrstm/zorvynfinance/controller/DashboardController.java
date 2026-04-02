package org.mrstm.zorvynfinance.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.mrstm.zorvynfinance.dto.Dashboard.DashboardSummaryResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.TrendPointResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.TrendType;
import org.mrstm.zorvynfinance.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "recentLimit must be at least 1") @Max(value = 50, message = "recentLimit must not be greater than 50") int recentLimit,
            @RequestParam(defaultValue = "MONTHLY") TrendType trendType,
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "periods must be at least 1") @Max(value = 24, message = "periods must not be greater than 24") int periods
    ) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(
                currentUserId(principal),
                startDate,
                endDate,
                recentLimit,
                trendType,
                periods
        ));
    }

    @GetMapping("/totals")
    public ResponseEntity<?> getTotals(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getTotals(currentUserId(principal), startDate, endDate));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategoryTotals(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getCategoryTotals(currentUserId(principal), startDate, endDate));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivity(
            Principal principal,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "limit must be at least 1") @Max(value = 50, message = "limit must not be greater than 50") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(currentUserId(principal), limit));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<TrendPointResponse>> getTrends(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTHLY") TrendType trendType,
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "periods must be at least 1") @Max(value = 24, message = "periods must not be greater than 24") int periods
    ) {
        return ResponseEntity.ok(dashboardService.getTrends(currentUserId(principal), startDate, endDate, trendType, periods));
    }

    private String currentUserId(Principal principal) {
        return principal.getName();
    }
}
