package org.mrstm.zorvynfinance.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointResponse {
    private String label;
    private long income;
    private long expense;
    private long net;
}

