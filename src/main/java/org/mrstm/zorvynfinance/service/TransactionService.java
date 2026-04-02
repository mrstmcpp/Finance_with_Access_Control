package org.mrstm.zorvynfinance.service;

import org.mrstm.zorvynfinance.dto.Transaction.PaginatedTransactionResponse;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Transaction;
import org.mrstm.zorvynfinance.model.Type;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final MongoTemplate mongoTemplate;

    public TransactionService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PaginatedTransactionResponse<Transaction> getFilteredTransactions(
            String userId,
            Type type,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));

        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(
                    Criteria.where("date")
                            .gte(startDate)
                            .lte(endDate)
            );
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

}
