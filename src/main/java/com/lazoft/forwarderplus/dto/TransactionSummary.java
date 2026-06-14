package com.lazoft.forwarderplus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    private long totalCount;
    private BigDecimal totalAmount;
    private long expenseCount;
    private BigDecimal expenseAmount;
    private long incomeCount;
    private BigDecimal incomeAmount;
}
