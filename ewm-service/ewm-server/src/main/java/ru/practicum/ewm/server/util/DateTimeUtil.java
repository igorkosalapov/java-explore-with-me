package ru.practicum.ewm.server.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parse(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }
}
