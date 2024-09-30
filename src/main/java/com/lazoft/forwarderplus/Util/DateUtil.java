package com.lazoft.forwarderplus.Util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

}
