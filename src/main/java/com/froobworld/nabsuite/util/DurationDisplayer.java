package com.froobworld.nabsuite.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DurationDisplayer {

    private DurationDisplayer() {}

    public static String asDurationString(long timeMillis) {
        return asDurationString(timeMillis, 2);
    }

    public static String asDurationString(long timeMillis, int numberOfUnits) {
        long days = TimeUnit.MILLISECONDS.toDays(timeMillis);
        timeMillis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(timeMillis);
        timeMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis);
        timeMillis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        if (days + hours + minutes == 0) {
            return "less than a minute";
        }
        List<String> components = new ArrayList<>();
        if (days > 0 && (numberOfUnits < 0 || components.size() < numberOfUnits)) {
            components.add(NumberDisplayer.toStringWithModifier((int) days, " day", " days", false));
        }
        if (hours > 0 && (numberOfUnits < 0 || components.size() < numberOfUnits)) {
            components.add(NumberDisplayer.toStringWithModifier((int) hours, " hour", " hours", false));
        }
        if (minutes > 0 && (numberOfUnits < 0 || components.size() < numberOfUnits)) {
            components.add(NumberDisplayer.toStringWithModifier((int) minutes, " minute", " minutes", false));
        }
        if (seconds > 0 && (numberOfUnits < 0 || components.size() < numberOfUnits)) {
            components.add(NumberDisplayer.toStringWithModifier((int) seconds, " second", " seconds", false));
        }
        int numberOfComponents = components.size();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfComponents; i++) {
            String prefix = i == 0 ? "" : i == numberOfComponents - 1 ? " and " : ", ";
            result.append(prefix).append(components.get(i));
        }
        return result.toString();
    }

}
