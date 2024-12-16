package com.lazoft.forwarderplus.util;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Stack;

public class AmountFormatter {

    static final Logger logger = LogManager.getLogger(AmountFormatter.class.getName());
    private static final String ZERO_AMOUNT = "0.00";
    private static final int DIGIT_INTERVAL_AFTER_FIRST_COMMA = 2;
    private static final int DIGIT_INTERVAL_BEFORE_FIRST_COMMA = 3;


    public static String getFormattedAmount(BigDecimal bigDecimal, AmountCurrency currency) {
        if (currency == AmountCurrency.BDT || currency == AmountCurrency.INR) {
            return getBDTakaFormattedAmount(bigDecimal);
        } else {
            return getForeignCurrencyFormatter().format(bigDecimal);
        }
    }

    public static String getBDTakaFormattedAmount(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return ZERO_AMOUNT;
        }
        try {
            String basicFormatted = getBasicFormatter().format(bigDecimal);
            String[] decimalSplitArray = basicFormatted.split("\\.");
            return separateWithCommaBDCurrency(decimalSplitArray[0]) + "." + decimalSplitArray[1];
        } catch (Exception e) {
            logger.error("ERROR while formatting amount: " + e.getMessage());
            return getForeignCurrencyFormatter().format(bigDecimal); //if exception, return built in formatted value
        }
    }

    private static String separateWithCommaBDCurrency(String amount) {
        boolean isNegative = false;
        String unsignedAmount;
        if (amount.startsWith("-")) {
            unsignedAmount = amount.substring(1); //
            isNegative = true;
        } else {
            unsignedAmount = amount;
        }
        if (unsignedAmount.length() < 4) { // means -1000 < amount < 1000; thus no need comma
            return amount;
        }
        return (isNegative ? "-" : "") + formatAmount(unsignedAmount);
    }

    private static String formatAmount(String s) {
        int charCount = DIGIT_INTERVAL_BEFORE_FIRST_COMMA;
        Stack<Character> charStack = new Stack<>();

        for (int i = s.length() - 1; i > -1; i--) {
            if (charCount == 0) {
                charStack.push(',');
                charCount = DIGIT_INTERVAL_AFTER_FIRST_COMMA;
            }
            charStack.push(s.charAt(i));
            charCount--;
        }
        return getFormattedAmountFromStack(charStack);
    }

    private static String getFormattedAmountFromStack(Stack<Character> charStack) {
        StringBuilder sb = new StringBuilder();
        while (!charStack.isEmpty()) {
            sb.append(charStack.pop());
        }
        return sb.toString();
    }

    public static DecimalFormat getBasicFormatter() {
        return new DecimalFormat("###0.00");
    }

    public static DecimalFormat getForeignCurrencyFormatter() {
        return new DecimalFormat("#,###0.00");
    }
}