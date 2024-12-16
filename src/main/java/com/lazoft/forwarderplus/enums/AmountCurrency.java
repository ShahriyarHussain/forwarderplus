package com.lazoft.forwarderplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmountCurrency {
    GBP("Pound", "£"),
    EUR("Euro", "€"),
    USD("US Dollar", "$"),
    AUD("Australian Dollar", "$"),
    CAD("Canadian Dollar", "$"),
    CNY("Chinese Yuan", "¥"),
    INR("Indian Rupee", "₹"),
    BDT("Taka", "৳"),
    JPY("Japanese Yen", "¥");

    private final String currencyName;
    private final String symbol;
}
