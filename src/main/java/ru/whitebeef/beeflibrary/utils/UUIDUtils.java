package ru.whitebeef.beeflibrary.utils;

import java.math.BigInteger;
import java.util.UUID;

public class UUIDUtils {
    public static UUID intToUuid(int i) {
        return UUID.nameUUIDFromBytes(BigInteger.valueOf(i).toByteArray());
    }

    public static long[] uuidToInt(UUID uuid) {
        return new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()};
    }
}
