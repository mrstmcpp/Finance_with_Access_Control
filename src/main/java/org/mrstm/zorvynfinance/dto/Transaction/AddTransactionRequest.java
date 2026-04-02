package org.mrstm.zorvynfinance.dto.Transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class AddTransactionRequest {
    @NotNull(message = "Transaction type is required")
    private Type type;

    @NotNull(message = "Transaction category is required")
    private Category category;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;

    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Transaction amount must be greater than 0")
    private Long amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}
