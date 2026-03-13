package io.fleetcoreplatform.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class IoTJobSchedulerValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Validates startTime based on AWS IoT requirements:
     * 1. Format: YYYY-MM-DD HH:MM
     * 2. Minimum 30 minutes from current time
     * 3. Maximum 1 year in advance
     */
    public static boolean isValidStartTime(String startTimeStr) {
        if (startTimeStr == null) return false;

        try {
            LocalDateTime scheduledTime = LocalDateTime.parse(startTimeStr, FORMATTER);
            LocalDateTime now = LocalDateTime.now();

            if (scheduledTime.isBefore(now.plusMinutes(30))) {
                return false;
            }

            return !scheduledTime.isAfter(now.plusYears(1));
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}