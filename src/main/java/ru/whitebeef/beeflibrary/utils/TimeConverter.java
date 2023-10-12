package ru.whitebeef.beeflibrary.utils;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimeConverter {
    public static final List<String> digits;
    private static final Map<String, Duration> timeUnits;
    private static final Pattern timePattern = Pattern.compile("\\G([0-9]+[hmsdwy])");

    static {
        digits = Arrays.stream("123456789".split("")).collect(Collectors.toList());
        timeUnits = new HashMap<>();
        timeUnits.put("h", Duration.ofHours(1));
        timeUnits.put("m", Duration.ofMinutes(1));
        timeUnits.put("s", Duration.ofSeconds(1));
        timeUnits.put("d", Duration.ofDays(1));
        timeUnits.put("w", Duration.ofDays(7));
        timeUnits.put("y", Duration.ofSeconds(31556952));

    }

    public static List<String> getTimeComplete(String[] args, int argNum) {
        argNum--;
        if (args[argNum].isEmpty()) {
            return digits;
        }
        String lastChar = args[argNum].split("")[args[argNum].length() - 1];
        if (digits.contains(lastChar)) {
            return prefix(args[argNum], timeUnits.keySet());
        } else if (timeUnits.containsKey(lastChar)) {
            return prefix(args[argNum], digits);
        } else {
            return new ArrayList<>();
        }
    }

    @Nullable
    public static LocalDateTime parseTime(String s) {
        Duration duration = Duration.ZERO;
        Matcher matcher = timePattern.matcher(s);
        while (matcher.find()) {
            String group = matcher.group();
            Duration d = timeUnits.get(group.split("")[group.length() - 1]);
            int multiplier = Integer.parseInt(group.substring(0, group.length() - 1));
            duration = duration.plus(d.multipliedBy(multiplier));
        }
        return duration.equals(Duration.ZERO) ? null : LocalDateTime.now().plus(duration);
    }


    @Nullable
    public static LocalDateTime parseTime(String time, long fromMillis) {
        Duration duration = Duration.ZERO;
        Matcher matcher = timePattern.matcher(time);
        while (matcher.find()) {
            String group = matcher.group();
            Duration d = timeUnits.get(group.split("")[group.length() - 1]);
            int multiplier = Integer.parseInt(group.substring(0, group.length() - 1));
            duration = duration.plus(d.multipliedBy(multiplier));
        }
        return duration.equals(Duration.ZERO) ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(fromMillis), ZoneId.systemDefault()).plus(duration);
    }

    @Nullable
    public static long parseTimeToSeconds(String time) {
        Duration duration = Duration.ZERO;
        Matcher matcher = timePattern.matcher(time);
        while (matcher.find()) {
            String group = matcher.group();
            Duration d = timeUnits.get(group.split("")[group.length() - 1]);
            int multiplier = Integer.parseInt(group.substring(0, group.length() - 1));
            duration = duration.plus(d.multipliedBy(multiplier));
        }
        return duration.toMillis() / 1000;
    }

    public static List<String> prefix(String prefix, Collection<String> collection) {
        return collection.stream().map(s -> prefix + s).collect(Collectors.toList());
    }

    /**
     * @param time String in format 1d2h3m4s
     * @return 0 if time is incorrect
     */
    public static long convert(String time) {
        Duration duration = Duration.ZERO;
        Matcher matcher = timePattern.matcher(time);
        while (matcher.find()) {
            String group = matcher.group();
            Duration d = timeUnits.get(group.split("")[group.length() - 1]);
            int multiplier = Integer.parseInt(group.substring(0, group.length() - 1));
            duration = duration.plus(d.multipliedBy(multiplier));
        }
        return duration.equals(Duration.ZERO) ? 0 : duration.toMillis();
    }

    /**
     * @return String in format 1d2h3m4s
     */
    public static String convertToString(long time, String regex) {
        StringBuilder sb = new StringBuilder();
        for (String unit : new String[]{"y", "w", "d", "h", "m", "s"}) {
            Duration duration = timeUnits.get(unit);
            int mul = (int) (time / duration.toMillis());
            time -= mul * duration.toMillis();
            if (mul != 0)
                sb.append(mul).append(unit).append(regex);
        }
        return sb.toString();
    }
}