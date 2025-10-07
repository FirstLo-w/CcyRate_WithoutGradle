package org.Main;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy hh24:mm:ss");

    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDate parseDateTime(String dateStr) throws DateTimeParseException {
        return LocalDate.parse(dateStr, DATETIME_FORMATTER);
    }

    public static String getCurrentDateStr() throws DateTimeException {
        return formatDate(LocalDate.now(), DateTimeFormatter.ofPattern("dd.MM.yyyy hh24:mm:ss"));
    }

    public static String formatDate(LocalDate date) {
        return formatDate(date, DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDate date) {
        return formatDate(date, DATETIME_FORMATTER);
    }

    public static String formatDate(LocalDate date, DateTimeFormatter format) {
        return date.format(format);
    }

    public static List<LocalDate> getDates(String inputDate) {
        List<LocalDate> dateList = new ArrayList<>();
        String[] days = inputDate.split("-");
        if (days.length == 2) {
            LocalDate startDate = parseDate(days[0]);
            LocalDate endDate = parseDate(days[1]);
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date must be before end date");
            }

            LocalDate currentDay = startDate;
            while (!currentDay.isAfter(endDate)) {
                dateList.add(currentDay);
                currentDay = currentDay.plusDays(1);
            }

        } else if (days.length == 1) {
            dateList.add(parseDate(inputDate));
        } else {
            throw new IllegalArgumentException("Invalid date range format");
        }

        return dateList;
    }
}
