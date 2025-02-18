package com.lazoft.forwarderplus.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

    public static String getCurrentDateAsString() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy").withLocale(Locale.ENGLISH));
    }

    public static String getDateAsString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy").withLocale(Locale.ENGLISH));
    }

}
