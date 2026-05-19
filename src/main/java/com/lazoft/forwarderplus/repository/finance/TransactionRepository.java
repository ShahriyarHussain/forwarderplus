package com.lazoft.forwarderplus.repository.finance;

import com.lazoft.forwarderplus.entity.finance.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
