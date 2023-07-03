package ru.whitebeef.beeflibrary.utils;

import java.util.List;
import java.util.Optional;

public class Setting<T> {
    private final String key;
    private T value;

    public Setting(String key, T defaultValue) {
        this.key = key;
        this.value = defaultValue;
    }

    public void setValue(T newValue) {
        this.value = newValue;
    }

    public Optional<T> getValue() {
        return Optional.of(value);
    }

    public String getKey() {
        return key;
    }

    public Optional<Integer> getInt() {
        if (!(value instanceof Integer intValue)) {
            return Optional.empty();
        }
        return Optional.of(intValue);
    }

    public Optional<Long> getLong() {
        if (!(value instanceof Long longValue)) {
            return Optional.empty();
        }
        return Optional.of(longValue);
    }

    public Optional<Double> getDouble() {
        if (!(value instanceof Double doubleValue)) {
            return Optional.empty();
        }
        return Optional.of(doubleValue);
    }

    public Optional<Boolean> getBoolean() {
        if (!(value instanceof Boolean booleanValue)) {
            return Optional.empty();
        }
        return Optional.of(booleanValue);
    }

    public Optional<String> getString() {
        if (!(value instanceof String stringValue)) {
            return Optional.empty();
        }
        return Optional.of(stringValue);
    }

    public Optional<List> getStringList() {
        if (!(value instanceof List listValue)) {
            return Optional.empty();
        }
        if (!(listValue.stream().findAny().orElse(null) instanceof String)) {
            return Optional.empty();
        }
        return Optional.of(listValue);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}