package org.mrstm.zorvynfinance.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.mrstm.zorvynfinance.dto.Transaction.AddTransactionRequest;
import org.mrstm.zorvynfinance.dto.Transaction.UpdateTransactionRequest;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Type;
import org.mrstm.zorvynfinance.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(
            Principal principal,
            @PathVariable @NotBlank(message = "transactionId is required") String transactionId
    ) {
        return ResponseEntity.ok(transactionService.getTransactionById(currentUserId(principal), transactionId));
    }

    @PostMapping
    public ResponseEntity<?> addTransaction(
            Principal principal,
            @Valid @RequestBody AddTransactionRequest transactionRequest
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(currentUserId(principal), transactionRequest));
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(
            Principal principal,
            @PathVariable @NotBlank(message = "transactionId is required") String transactionId,
            @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(currentUserId(principal), transactionId, updateTransactionRequest));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(
            Principal principal,
            @PathVariable @NotBlank(message = "transactionId is required") String transactionId
    ) {
        transactionService.deleteTransaction(currentUserId(principal), transactionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            Principal principal,
            @RequestParam(required = false) Type type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) @Size(max = 100, message = "search must not be greater than 100 characters") String search,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size must not be greater than 100") int size
    ){
        return ResponseEntity.ok(
                transactionService.getFilteredTransactions(currentUserId(principal), type, category, startDate, endDate, search, page, size)
        );
    }

    private String currentUserId(Principal principal) {
        return principal.getName();
    }
}
