package com.rsoft.hurmanagement.hurmasterdata.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DateTimeRangeSupport {

    private DateTimeRangeSupport() {
    }

    public static long computeMinutes(LocalDate startDate, String startTime, LocalDate endDate, String endTime) {
        if (startDate == null || endDate == null || isBlank(startTime) || isBlank(endTime)) {
            return 0L;
        }
        try {
            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.parse(startTime));
            LocalDateTime end = LocalDateTime.of(endDate, LocalTime.parse(endTime));
            long minutes = Duration.between(start, end).toMinutes();
            return Math.max(minutes, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    public static long computeMinutesFromTimes(String startTime, String endTime, boolean allowOvernight) {
        if (isBlank(startTime) || isBlank(endTime)) {
            return 0L;
        }
        try {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            long minutes = Duration.between(start, end).toMinutes();
            if (minutes < 0 && allowOvernight) {
                minutes += 24 * 60;
            }
            return Math.max(minutes, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
