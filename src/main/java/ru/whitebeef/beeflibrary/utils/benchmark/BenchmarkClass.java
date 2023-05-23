package ru.whitebeef.beeflibrary.utils.benchmark;

import ru.whitebeef.beeflibrary.BeefLibrary;

public class BenchmarkClass {

    private final long timeStart;

    public BenchmarkClass() {
        timeStart = System.nanoTime();
    }

    public static void benchAppend(StringBuilder sb, long value, String text) {
        if (value > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(value).append(text);
        }
    }

    public void printTime(String nameFunction) {
        printTime(nameFunction, timeStart, System.nanoTime());
    }

    public void printTime(String nameFunction, long timeStart, long timeEnd) {
        if (!BeefLibrary.getInstance().isDebug()) {
            return;
        }
        long remainingTime = timeEnd - timeStart;
        StringBuilder sb = new StringBuilder();
        long seconds = remainingTime / 1000000000;
        long days = seconds / (3600 * 24);
        benchAppend(sb, days, "d");
        seconds -= (days * 3600 * 24);
        long hours = seconds / 3600;
        benchAppend(sb, hours, "h");
        seconds -= (hours * 3600);
        long minutes = seconds / 60;
        benchAppend(sb, minutes, "m");
        seconds -= (minutes * 60);
        benchAppend(sb, seconds, "s");
        long milliseconds = remainingTime / 1000000 - seconds * 1000;
        benchAppend(sb, milliseconds, "ms");
        long nanos = remainingTime % 1000000;
        benchAppend(sb, nanos, "ns");

        BeefLibrary.getInstance().getSLF4JLogger().info("{}:\t{}", nameFunction, sb);
    }

}