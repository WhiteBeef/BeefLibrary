package ru.whitebeef.beeflibrary.utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FlagSetting implements Cloneable {

    public static Builder builder() {
        return new Builder();
    }

    private Map<String, Integer> settings = new HashMap<>();
    private BitSet bitSet = new BitSet();

    private FlagSetting(@NotNull Set<String> settings) {
        settings.forEach(setting -> this.settings.put(setting, this.settings.size()));
    }

    public void setSetting(@NotNull String namespace, boolean value) {
        if (!settings.containsKey(namespace)) {
            throw new IllegalArgumentException("Namespace '" + namespace + "' is not registered!");
        }
        bitSet.set(settings.get(namespace), value);
    }

    public static BitSet convertTo(BigInteger bi) {
        byte[] bia = bi.toByteArray();
        int l = bia.length;
        byte[] bsa = new byte[l + 1];
        System.arraycopy(bia, 0, bsa, 0, l);
        bsa[l] = 0x01;
        return BitSet.valueOf(bsa);
    }

    public static BigInteger convertFrom(BitSet bs) {
        byte[] bsa = bs.toByteArray();
        int l = bsa.length - 0x01;
        byte[] bia = new byte[l];
        System.arraycopy(bsa, 0, bia, 0, l);
        return new BigInteger(bia);
    }

    /**
     * @return false if setting is undefined
     */
    public boolean getSetting(String namespace) {
        if (!settings.containsKey(namespace)) {
            return false;
        }
        return bitSet.get(settings.get(namespace));
    }

    @Override
    public FlagSetting clone() {
        try {
            FlagSetting clone = (FlagSetting) super.clone();
            clone.settings = new HashMap<>(settings);
            clone.bitSet = (BitSet) bitSet.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class Builder {
        private final Set<String> settings = new LinkedHashSet<>();

        public Builder addSetting(@NotNull String namespace) {
            settings.add(namespace);
            return this;
        }

        @NotNull
        public FlagSetting build() {
            return new FlagSetting(settings);
        }
    }

}
