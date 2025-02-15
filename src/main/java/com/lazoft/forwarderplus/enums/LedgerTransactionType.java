package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LedgerTransactionType {
    FOLLOW_ACC_TRAN("Ledger Transaction Follow Transaction"),
    FOLLOW_REVERSE_ACC_TRAN("Ledger Transaction Follow Reverse Transaction"),
    ONLY_ACC_DEBIT_TRAN("Ledger Transaction Follow Only Expense Transaction"),
    ONLY_ACC_CREDIT_TRAN("Ledger Transaction Follow Only Income Transaction"),
    CHOOSE_ON_TRAN("Choose During Transaction Posting");

    private final String title;
}
