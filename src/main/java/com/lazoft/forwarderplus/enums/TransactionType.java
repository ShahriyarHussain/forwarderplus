package com.lazoft.forwarderplus.enums;

import com.lazoft.forwarderplus.entity.Transaction;

public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER;

    public TransactionType getOpposite() {
        if (this == TransactionType.EXPENSE) {
            return INCOME;
        }
        return EXPENSE;
    }
}
