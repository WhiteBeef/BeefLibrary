package ru.whitebeef.beeflibrary.utils;

import org.apache.commons.lang3.RandomUtils;

public class MathUtils {

    public static Integer getInt(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            try {
                String[] arr = line.split("\\.\\.");
                return RandomUtils.nextInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) + 1);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

}
