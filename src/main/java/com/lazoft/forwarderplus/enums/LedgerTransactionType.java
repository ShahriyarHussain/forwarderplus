package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LedgerTransactionType {
    FOLLOW_ACC_TRAN("Account Transaction as it is"),
    FOLLOW_REVERSE_ACC_TRAN("Account Transaction as it is but Reverse"),
    ONLY_ACC_DEBIT_TRAN("Only Expense Transaction"),
    ONLY_ACC_CREDIT_TRAN("Only Income Transactions"),
    CHOOSE_ON_TRAN("Choose During Transaction");

    private final String title;
}
