package org.mrstm.zorvynfinance.dto.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Type;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private Type type;
    private Category category;
    private LocalDate date;
    private long amount;
    private String description;
}

