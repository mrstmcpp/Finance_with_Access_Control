package org.mrstm.zorvynfinance.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalIncome;
    private long totalExpense;
    private long netBalance;
    private List<CategoryTotalResponse> categoryTotals;
    private List<RecentActivityResponse> recentActivity;
    private List<TrendPointResponse> trends;
}

