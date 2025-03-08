package com.lazoft.forwarderplus.enums;

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
