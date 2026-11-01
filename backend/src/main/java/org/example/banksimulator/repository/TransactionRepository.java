package org.example.banksimulator.repository;

import org.example.banksimulator.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN t.sourceAccount s
            LEFT JOIN t.destinationAccount d
            WHERE s.id = :accountId OR d.id = :accountId
            """)
    Page<Transaction> findHistoryByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
