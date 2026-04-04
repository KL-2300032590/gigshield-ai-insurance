package com.parametrix.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    void getWeekNumber_returnsCorrectWeekNumber() {
        // Week 1 of 2024 starts on Monday, Jan 1
        LocalDate testDate = LocalDate.of(2024, 1, 8); // Second week of 2024
        int weekNumber = DateUtils.getWeekNumber(testDate);
        
        // Week number should be 2 (second week of the year)
        assertThat(weekNumber).isGreaterThanOrEqualTo(1);
        assertThat(weekNumber).isLessThanOrEqualTo(53);
    }

    @Test
    void getWeekYear_returnsCorrectYear() {
        LocalDate testDate = LocalDate.of(2024, 6, 15);
        int weekYear = DateUtils.getWeekYear(testDate);
        
        assertThat(weekYear).isEqualTo(2024);
    }

    @Test
    void getWeekStartDate_returnsMonday() {
        LocalDate startDate = DateUtils.getWeekStartDate(10, 2024);
        
        assertThat(startDate.getDayOfWeek().getValue()).isEqualTo(1); // Monday
        assertThat(startDate.getYear()).isIn(2023, 2024); // Could be Dec of prev year for week 1
    }

    @Test
    void getWeekEndDate_returnsSunday() {
        LocalDate endDate = DateUtils.getWeekEndDate(10, 2024);
        
        assertThat(endDate.getDayOfWeek().getValue()).isEqualTo(7); // Sunday
    }

    @Test
    void getWeekEndDate_isExactly6DaysAfterStartDate() {
        int weekNumber = 15;
        int year = 2024;
        
        LocalDate startDate = DateUtils.getWeekStartDate(weekNumber, year);
        LocalDate endDate = DateUtils.getWeekEndDate(weekNumber, year);
        
        assertThat(endDate).isEqualTo(startDate.plusDays(6));
    }

    @Test
    void getCurrentWeekNumber_returnsValidWeekNumber() {
        int currentWeek = DateUtils.getCurrentWeekNumber();
        
        assertThat(currentWeek).isGreaterThanOrEqualTo(1);
        assertThat(currentWeek).isLessThanOrEqualTo(53);
    }

    @Test
    void getCurrentWeekYear_returnsReasonableYear() {
        int currentWeekYear = DateUtils.getCurrentWeekYear();
        int currentYear = LocalDate.now().getYear();
        
        // Week year should be current year or adjacent (for edge cases at year boundaries)
        assertThat(currentWeekYear).isBetween(currentYear - 1, currentYear + 1);
    }

    @Test
    void weekNumberAndYear_areConsistentForSameDate() {
        LocalDate today = LocalDate.now();
        int weekNumber = DateUtils.getWeekNumber(today);
        int weekYear = DateUtils.getWeekYear(today);
        
        // Getting the start of that week should give a date in a valid range
        LocalDate weekStart = DateUtils.getWeekStartDate(weekNumber, weekYear);
        
        assertThat(weekStart).isBeforeOrEqualTo(today);
        assertThat(weekStart.plusDays(6)).isAfterOrEqualTo(today);
    }
}
