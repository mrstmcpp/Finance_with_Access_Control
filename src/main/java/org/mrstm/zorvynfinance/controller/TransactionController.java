package org.mrstm.zorvynfinance.controller;


import jakarta.validation.Valid;
import org.mrstm.zorvynfinance.dto.Transaction.AddTransactionRequest;
import org.mrstm.zorvynfinance.dto.Transaction.UpdateTransactionRequest;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Type;
import org.mrstm.zorvynfinance.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    private ResponseEntity<?> getTransaction(@PathVariable String transactionId){
        return null;
    }

    @PostMapping("/")
    private ResponseEntity<?> addTransaction(@Valid @RequestBody AddTransactionRequest transactionRequest){
        return null;
    }

    @PatchMapping("/{transactionId}")
    private ResponseEntity<?> updateTransaction(@PathVariable String transactionId , @Valid @RequestBody UpdateTransactionRequest updateTransactionRequest){
        return null;
    }

    @DeleteMapping("/{transactionId}")
    private ResponseEntity<?> deleteTransaction(@PathVariable String transactionId){
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) Type type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        // username is eq to userId
        String userId = Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getName();
//        System.out.println(userId);
        return ResponseEntity.ok(
                transactionService.getFilteredTransactions(userId, type, category, startDate, endDate, page, size)
        );
    }
}
