package com.lazoft.forwarderplus.dto;

import com.lazoft.forwarderplus.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResult {
    private Page<Transaction> transactions;
    private TransactionSummary summary;
    // getters/setters
}