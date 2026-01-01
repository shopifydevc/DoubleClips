package com.vanvatcorporation.doubleclips.helper;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberHelper {
    public static String abbreviateNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        }
        // Determine the magnitude of the number
        int exp = (int) (Math.log10(number) / 3);
        // Get the appropriate abbreviation
        String[] suffixes = {"k", "M", "B", "T"};
        String suffix = exp > 0 && exp <= suffixes.length? suffixes[exp - 1] : "";
        // Calculate the abbreviated value
        double value = number / Math.pow(1000, exp);
        // Format the value to have at most 1 decimal place
        return String.format("%.1f%s", value, suffix).replaceAll("\\.0", "");
    }
}
