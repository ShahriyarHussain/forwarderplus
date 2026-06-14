package com.lazoft.forwarderplus.dto;

import com.lazoft.forwarderplus.enums.AccountType;

import java.math.BigDecimal;

public record AccountTypeSummaryDto(
        AccountType accountType,
        long totalTransaction,
        BigDecimal totalDebit,
        BigDecimal totalCredit
) {

    public BigDecimal getNetBalance() {
        if (accountType == AccountType.ASSET || accountType == AccountType.EXPENSE) {
            return totalDebit.subtract(totalCredit);
        } else if (accountType == AccountType.LIABILITY
                || accountType == AccountType.REVENUE
                || accountType == AccountType.EQUITY) {
            return totalCredit.subtract(totalDebit);
        }
        return BigDecimal.ZERO;
    }
}
