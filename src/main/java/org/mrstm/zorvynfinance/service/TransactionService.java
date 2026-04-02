package org.mrstm.zorvynfinance.service;

import org.mrstm.zorvynfinance.dto.Transaction.AddTransactionRequest;
import org.mrstm.zorvynfinance.dto.Transaction.PaginatedTransactionResponse;
import org.mrstm.zorvynfinance.dto.Transaction.UpdateTransactionRequest;
import org.mrstm.zorvynfinance.exception.InvalidOperationException;
import org.mrstm.zorvynfinance.exception.TransactionNotFoundException;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Transaction;
import org.mrstm.zorvynfinance.model.Type;
import org.mrstm.zorvynfinance.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final MongoTemplate mongoTemplate;
    private final TransactionRepository transactionRepository;

    public TransactionService(MongoTemplate mongoTemplate, TransactionRepository transactionRepository) {
        this.mongoTemplate = mongoTemplate;
        this.transactionRepository = transactionRepository;
    }

    public Transaction getTransactionById(String userId, String transactionId) {
        return transactionRepository.findByIdAndUserIdAndDeletedFalse(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    public Transaction addTransaction(String userId, AddTransactionRequest transactionRequest) {
        Transaction transaction = Transaction.builder()
                .type(transactionRequest.getType())
                .category(transactionRequest.getCategory())
                .date(transactionRequest.getDate())
                .description(transactionRequest.getDescription())
                .amount(transactionRequest.getAmount())
                .userId(userId)
                .deleted(false)
                .build();
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(String userId, String transactionId, UpdateTransactionRequest updateTransactionRequest) {
        Transaction transaction = getTransactionById(userId, transactionId);

        if (updateTransactionRequest.getType() != null) {
            transaction.setType(updateTransactionRequest.getType());
        }
        if (updateTransactionRequest.getCategory() != null) {
            transaction.setCategory(updateTransactionRequest.getCategory());
        }
        if (updateTransactionRequest.getDate() != null) {
            transaction.setDate(updateTransactionRequest.getDate());
        }
        if (updateTransactionRequest.getAmount() != null) {
            transaction.setAmount(updateTransactionRequest.getAmount());
        }
        if (updateTransactionRequest.getDescription() != null) {
            transaction.setDescription(updateTransactionRequest.getDescription());
        }

        if (isNoUpdateRequested(updateTransactionRequest)) {
            throw new InvalidOperationException("At least one field must be provided for update");
        }

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(String userId, String transactionId) {
        Transaction transaction = getTransactionById(userId, transactionId);
        transaction.setDeleted(true);
        transaction.setDeletedAt(Instant.now());
        transactionRepository.save(transaction);
    }

    public PaginatedTransactionResponse<Transaction> getFilteredTransactions(
            String userId,
            Type type,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            int page,
            int size
    ) {
        validateDateRange(startDate, endDate);

        Query query = new Query();
        query.addCriteria(buildBaseCriteria(userId));

        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("date").gte(startDate).lte(endDate));
        }

        if (search != null && !search.isBlank()) {
            query.addCriteria(buildSearchCriteria(search));
        }

        long total = mongoTemplate.count(query, Transaction.class);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        query.with(pageable);

        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

        return PaginatedTransactionResponse.<Transaction>builder()
                .data(transactions)
                .currentPage(page)
                .pageSize(size)
                .totalRecords(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }

    private Criteria buildBaseCriteria(String userId) {
        return Criteria.where("userId").is(userId).and("deleted").ne(true);
    }

    private Criteria buildSearchCriteria(String search) {
        String normalized = search.trim();
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("description").regex(normalized, "i"));

        for (Category value : Category.values()) {
            if (value.name().toLowerCase().contains(normalized.toLowerCase())) {
                criteriaList.add(Criteria.where("category").is(value));
            }
        }

        for (Type value : Type.values()) {
            if (value.name().toLowerCase().contains(normalized.toLowerCase())) {
                criteriaList.add(Criteria.where("type").is(value));
            }
        }

        return new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException("startDate and endDate must be provided together");
        }

        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
    }

    private boolean isNoUpdateRequested(UpdateTransactionRequest updateTransactionRequest) {
        return updateTransactionRequest.getType() == null
                && updateTransactionRequest.getCategory() == null
                && updateTransactionRequest.getDate() == null
                && updateTransactionRequest.getAmount() == null
                && updateTransactionRequest.getDescription() == null;
    }

}
