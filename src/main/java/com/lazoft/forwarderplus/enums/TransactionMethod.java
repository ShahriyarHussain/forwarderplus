package com.lazoft.forwarderplus.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionMethod {
    ELECTRONIC_PAYMENT("Electronic Payment(RTGS/BEFTN/NPSB)"),
    CHEQUE("Cheque"),
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_WALLET("Mobile Wallet"),
    REMITTANCE("Remittance"),
    DEBIT_CREDIT_CARD("Debit/Credit Card");

    private final String label;
}
