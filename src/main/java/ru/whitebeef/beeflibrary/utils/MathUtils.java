package ru.whitebeef.beeflibrary.utils;


import java.util.Random;

public class MathUtils {
    private static final Random random = new Random();

    public static Integer getInt(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            try {
                String[] arr = line.split("\\.\\.");
                return random.nextInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) + 1);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

}
