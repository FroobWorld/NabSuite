package com.froobworld.nabsuite.util;

import java.util.concurrent.TimeUnit;

public final class DurationParser {

    private DurationParser() {}

    public static long fromString(String durationString) {
        long duration = 0;
        StringBuilder currentSubDuration = new StringBuilder();
        for (char c : durationString.toCharArray()) {
            if (Character.isDigit(c)) {
                currentSubDuration.append(c);
            } else {
                TimeUnit unit = switch (c) {
                    case 'd' -> TimeUnit.DAYS;
                    case 'h' -> TimeUnit.HOURS;
                    case 'm' -> TimeUnit.MINUTES;
                    default -> throw new IllegalArgumentException("Illegal unit: " + c);
                };
                duration += unit.toMillis(Long.parseLong(currentSubDuration.toString()));
                currentSubDuration = new StringBuilder();
            }
        }
        return duration;
    }

}
