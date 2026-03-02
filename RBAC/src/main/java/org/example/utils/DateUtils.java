package org.example.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        LocalDate parsed = LocalDate.parse(date, DATE_FORMAT);
        return parsed.plusDays(days).format(DATE_FORMAT);
    }

    public static String formatRelativeTime(String date) {
        if (date == null || date.isEmpty()) return "unknown";

        LocalDate target;
        try {
            target = LocalDate.parse(date.substring(0, 10), DATE_FORMAT);
        } catch (Exception e) {
            return "unknown";
        }

        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, target);

        if (days == 0) return "today";
        if (days == 1) return "in 1 day";
        if (days == -1) return "1 day ago";
        if (days > 1) return "in " + days + " days";
        return Math.abs(days) + " days ago";
    }
}
