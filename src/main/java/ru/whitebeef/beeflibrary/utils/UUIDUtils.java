package ru.whitebeef.beeflibrary.utils;

import java.math.BigInteger;
import java.util.UUID;

public class UUIDUtils {
    public static UUID intToUuid(int value) {
        return UUID.nameUUIDFromBytes(BigInteger.valueOf(value).toByteArray());
    }

    public static UUID longToUuid(long value) {
        return new UUID(0, value);
    }

    public static long[] uuidToLongArr(UUID uuid) {
        return new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()};
    }

    public static long uuidToLeastLong(UUID uuid) {
        return uuid.getLeastSignificantBits();
    }
}
