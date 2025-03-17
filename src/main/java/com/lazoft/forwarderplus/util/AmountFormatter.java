package com.lazoft.forwarderplus.util;

import com.lazoft.forwarderplus.enums.AmountCurrency;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class AmountFormatter {

    static final Logger logger = LogManager.getLogger(AmountFormatter.class.getName());
    private static final String ZERO_AMOUNT = "0.00";
    private static final int DIGIT_INTERVAL_AFTER_FIRST_COMMA = 2;
    private static final int DIGIT_INTERVAL_BEFORE_FIRST_COMMA = 3;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    private static final BigDecimal LAKH = BigDecimal.valueOf(100000);
    private static final BigDecimal CRORE = BigDecimal.valueOf(10000000);

    private static final Map<String, String> map = Map.ofEntries(
            Map.entry("0", ""),
            Map.entry("1", "One"),
            Map.entry("2", "Two"),
            Map.entry("3", "Three"),
            Map.entry("4", "Four"),
            Map.entry("5", "Five"),
            Map.entry("6", "Six"),
            Map.entry("7", "Seven"),
            Map.entry("8", "Eight"),
            Map.entry("9", "Nine"),
            Map.entry("10", "Ten"),
            Map.entry("11", "Eleven"),
            Map.entry("12", "Twelve"),
            Map.entry("13", "Thirteen"),
            Map.entry("14", "Fourteen"),
            Map.entry("15", "Fifteen"),
            Map.entry("16", "Sixteen"),
            Map.entry("17", "Seventeen"),
            Map.entry("18", "Eighteen"),
            Map.entry("19", "Nineteen"),
            Map.entry("20", "Twenty"),
            Map.entry("30", "Thirty"),
            Map.entry("40", "Forty"),
            Map.entry("50", "Fifty"),
            Map.entry("60", "Sixty"),
            Map.entry("70", "Seventy"),
            Map.entry("80", "Eighty"),
            Map.entry("90", "Ninety"));

    private AmountFormatter() {}


    public static String getFormattedAmount(BigDecimal bigDecimal, AmountCurrency currency) {
//        LocaleUtils.toLocale(Locale.ENGLISH);
        if (currency == AmountCurrency.BDT || currency == AmountCurrency.INR) {
            return getBDRegionFormattedAmount(bigDecimal);
        } else {
            return getForeignCurrencyFormatter().format(bigDecimal);
        }
    }

    public static String getBDRegionFormattedAmount(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return ZERO_AMOUNT;
        }
        try {
            String basicFormatted = getBasicFormatter().format(bigDecimal);
            String[] decimalSplitArray = basicFormatted.split("\\.");
            return separateWithCommaBDCurrency(decimalSplitArray[0]) + "." + decimalSplitArray[1];
        } catch (Exception e) {
            logger.error("ERROR while formatting amount: ", e);
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
        Deque<Character> charStack = new ArrayDeque<>();

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

    private static String getFormattedAmountFromStack(Deque<Character> charStack) {
        StringBuilder sb = new StringBuilder();
        while (!charStack.isEmpty()) {
            sb.append(charStack.pop());
        }
        return sb.toString();
    }
//
    public static DecimalFormat getBasicFormatter() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        decimalFormat.applyPattern("###0.00");
        return decimalFormat;
    }

    public static DecimalFormat getForeignCurrencyFormatter() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        decimalFormat.applyPattern("#,##0.00");
        return decimalFormat;
    }

//    public static DecimalFormat getBasicFormatter() {
//        return new DecimalFormat("###0.00");
//    }
//
//    public static DecimalFormat getForeignCurrencyFormatter() {
//        return new DecimalFormat("#,###0.00");
//    }

    public static String getAmountInWords(BigDecimal amount) {
        if (amount == null) {
            return "Zero ";
        }
        if (amount.toPlainString().contains(".")) {
            String [] splitAmount = amount.toPlainString().split("\\.");
            BigDecimal nonDecimalAmount = new BigDecimal(splitAmount[0]);
            BigDecimal decimalAmount = new BigDecimal(splitAmount[1]);
            String nonDecimalPart = getRoundedAmountInWords(nonDecimalAmount);
            String decimalPart = getRoundedAmountInWords(decimalAmount);
            decimalPart = decimalPart.equals("Zero") ? "" : decimalPart + " Paisa ";
            return nonDecimalPart + decimalPart;
        } else {
            return getRoundedAmountInWords(amount);
        }
    }

    private static String getRoundedAmountInWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Zero";
        }

        StringBuilder amountInWords = new StringBuilder();
        amount = amount.setScale(0, RoundingMode.DOWN);

        BigDecimal remainder = amount.divide(CRORE, 0, RoundingMode.DOWN);
        if (isNotZero(remainder)) {
            amountInWords.append(getRoundedAmountInWords(remainder)).append(" Crore ");
        }
        amount = amount.remainder(CRORE);

        remainder = amount.divide(LAKH, 0, RoundingMode.DOWN);
        if (isNotZero(remainder)) {
            amountInWords.append(getTwoDigitInWords(remainder)).append(" Lakh ");
        }
        amount = amount.remainder(LAKH);

        remainder = amount.divide(THOUSAND, 0, RoundingMode.DOWN);
        if (isNotZero(remainder)) {
            amountInWords.append(getTwoDigitInWords(remainder)).append(" Thousand ");
        }
        amount = amount.remainder(THOUSAND);

        remainder = amount.divide(HUNDRED, 0, RoundingMode.DOWN);
        if (isNotZero(remainder)) {
            amountInWords.append(getTwoDigitInWords(remainder)).append(" Hundred ");
        }
        amount = amount.remainder(HUNDRED);

        amountInWords.append(getTwoDigitInWords(amount));
        return amountInWords.toString();
    }

    private static boolean isNotZero(BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) != 0;
    }

    private static String getTwoDigitInWords(BigDecimal amount) {
        BigDecimal remainder = amount.divide(BigDecimal.TEN, 0, RoundingMode.DOWN);
        if (remainder.equals(BigDecimal.ONE)) {
            return map.get(amount.toPlainString());
        } else if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            return map.get(amount.remainder(BigDecimal.TEN).toPlainString());
        } else if (amount.remainder(BigDecimal.TEN).compareTo(BigDecimal.ZERO) == 0) {
            return map.get(remainder.multiply(BigDecimal.TEN).toPlainString());
        } else {
            return map.get(remainder.multiply(BigDecimal.TEN).toPlainString()) + " "
                    + map.get(amount.remainder(BigDecimal.TEN).toPlainString());
        }
    }
}