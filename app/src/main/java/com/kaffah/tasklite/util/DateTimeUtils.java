package com.kaffah.tasklite.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static long startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long startOfTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfToday());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    public static boolean isOverdue(Long dueAt, boolean completed) {
        return !completed && dueAt != null && dueAt < System.currentTimeMillis();
    }

    public static String formatDeadline(Long dueAt) {
        if (dueAt == null) {
            return "Tanpa deadline";
        }
        Date date = new Date(dueAt);
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
                + ", "
                + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
    }
}
