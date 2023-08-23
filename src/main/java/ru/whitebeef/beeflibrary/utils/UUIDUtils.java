package ru.whitebeef.beeflibrary.utils;

import java.math.BigInteger;
import java.util.UUID;

public class UUIDUtils {
    public static UUID intToUuid(int value) {
        return UUID.nameUUIDFromBytes(BigInteger.valueOf(value).toByteArray());
    }

    public static UUID longToUuid(long value) {
        return UUID.nameUUIDFromBytes(BigInteger.valueOf(value).toByteArray());
    }

    public static long[] uuidToInt(UUID uuid) {
        return new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()};
    }
}
