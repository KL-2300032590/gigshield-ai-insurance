package com.parametrix.common.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Utility class for date and week calculations.
 */
public final class DateUtils {
    
    private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.getDefault());
    
    private DateUtils() {
        // Utility class
    }
    
    /**
     * Get the ISO week number for a date.
     */
    public static int getWeekNumber(LocalDate date) {
        return date.get(WEEK_FIELDS.weekOfWeekBasedYear());
    }
    
    /**
     * Get the year of the week-based year.
     */
    public static int getWeekYear(LocalDate date) {
        return date.get(WEEK_FIELDS.weekBasedYear());
    }
    
    /**
     * Get the start date (Monday) of a week.
     */
    public static LocalDate getWeekStartDate(int weekNumber, int year) {
        return LocalDate.of(year, 1, 1)
                .with(WEEK_FIELDS.weekOfWeekBasedYear(), weekNumber)
                .with(DayOfWeek.MONDAY);
    }
    
    /**
     * Get the end date (Sunday) of a week.
     */
    public static LocalDate getWeekEndDate(int weekNumber, int year) {
        return getWeekStartDate(weekNumber, year).plusDays(6);
    }
    
    /**
     * Get the current week number.
     */
    public static int getCurrentWeekNumber() {
        return getWeekNumber(LocalDate.now());
    }
    
    /**
     * Get the current week-based year.
     */
    public static int getCurrentWeekYear() {
        return getWeekYear(LocalDate.now());
    }
}
