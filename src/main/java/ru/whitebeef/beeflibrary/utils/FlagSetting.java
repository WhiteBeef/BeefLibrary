package ru.whitebeef.beeflibrary.utils;

import org.jetbrains.annotations.NotNull;

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
