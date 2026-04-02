package org.mrstm.zorvynfinance.repository;

import org.mrstm.zorvynfinance.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Optional<Transaction> findByIdAndUserIdAndDeletedFalse(String id, String userId);
}

