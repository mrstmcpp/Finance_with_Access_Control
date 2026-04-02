package org.mrstm.zorvynfinance.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mrstm.zorvynfinance.model.Category;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTotalResponse {
    private Category category;
    private long income;
    private long expense;
    private long net;
}

