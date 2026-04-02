package org.mrstm.zorvynfinance.dto.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedTransactionResponse<T> {
    private List<T> data;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private int totalPages;
}
