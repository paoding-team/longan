package dev.paoding.longan.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {
    private static final int length = 19;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalTime parseTime(String time) {
        if (time.length() == length) {
            return LocalTime.parse(time, dateTimeformatter);
        } else {
            return LocalTime.parse(time, timeFormatter);
        }
    }

    public static LocalDate parseDate(String date) {
        if (date.length() == length) {
            return LocalDate.parse(date, dateTimeformatter);
        } else {
            return LocalDate.parse(date, dateFormatter);
        }
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, dateTimeformatter);
    }

    public static long getWeeks() {
        LocalDate currentDate = LocalDate.now();
        LocalDate initialDate = LocalDate.of(2019, 8, 5);
        return ChronoUnit.WEEKS.between(initialDate, currentDate);
    }

    public static long getWeeks(LocalDate date) {
        LocalDate initialDate = LocalDate.of(2019, 8, 5);
        return ChronoUnit.WEEKS.between(initialDate, date);
    }

    public static long getWeeks(LocalDateTime dateTime) {
        LocalDate initialDate = LocalDate.of(2019, 8, 5);
        return ChronoUnit.WEEKS.between(initialDate, dateTime);
    }
}
