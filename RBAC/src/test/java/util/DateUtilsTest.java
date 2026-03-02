package util;

import org.example.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void getCurrentDateFormat() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void getCurrentDateTimeFormat() {
        String dateTime = DateUtils.getCurrentDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void isBeforeTrue() {
        assertTrue(DateUtils.isBefore("2024-01-01", "2024-12-31"));
    }

    @Test
    void isBeforeFalse() {
        assertFalse(DateUtils.isBefore("2024-12-31", "2024-01-01"));
    }

    @Test
    void isBeforeEqual() {
        assertFalse(DateUtils.isBefore("2024-06-15", "2024-06-15"));
    }

    @Test
    void isAfterTrue() {
        assertTrue(DateUtils.isAfter("2024-12-31", "2024-01-01"));
    }

    @Test
    void isAfterFalse() {
        assertFalse(DateUtils.isAfter("2024-01-01", "2024-12-31"));
    }

    @Test
    void isAfterEqual() {
        assertFalse(DateUtils.isAfter("2024-06-15", "2024-06-15"));
    }

    @Test
    void addDaysPositive() {
        assertEquals("2024-01-11", DateUtils.addDays("2024-01-01", 10));
    }

    @Test
    void addDaysNegative() {
        assertEquals("2023-12-31", DateUtils.addDays("2024-01-01", -1));
    }

    @Test
    void addDaysZero() {
        assertEquals("2024-06-15", DateUtils.addDays("2024-06-15", 0));
    }

    @Test
    void addDaysCrossMonth() {
        assertEquals("2024-02-01", DateUtils.addDays("2024-01-31", 1));
    }

    @Test
    void addDaysCrossYear() {
        assertEquals("2025-01-01", DateUtils.addDays("2024-12-31", 1));
    }

    @Test
    void formatRelativeTimeToday() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("today", DateUtils.formatRelativeTime(today));
    }

    @Test
    void formatRelativeTimeTomorrow() {
        String tomorrow = LocalDate.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("in 1 day", DateUtils.formatRelativeTime(tomorrow));
    }

    @Test
    void formatRelativeTimeYesterday() {
        String yesterday = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("1 day ago", DateUtils.formatRelativeTime(yesterday));
    }

    @Test
    void formatRelativeTimeFuture() {
        String future = LocalDate.now().plusDays(5)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("in 5 days", DateUtils.formatRelativeTime(future));
    }

    @Test
    void formatRelativeTimePast() {
        String past = LocalDate.now().minusDays(3)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        assertEquals("3 days ago", DateUtils.formatRelativeTime(past));
    }

    @Test
    void formatRelativeTimeWithDateTime() {
        String dateTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + " 14:30:00";
        assertEquals("today", DateUtils.formatRelativeTime(dateTime));
    }

    @Test
    void formatRelativeTimeNull() {
        assertEquals("unknown", DateUtils.formatRelativeTime(null));
    }

    @Test
    void formatRelativeTimeEmpty() {
        assertEquals("unknown", DateUtils.formatRelativeTime(""));
    }

    @Test
    void formatRelativeTimeInvalid() {
        assertEquals("unknown", DateUtils.formatRelativeTime("not-a-date"));
    }
}