package ru.whitebeef.beeflibrary.utils;

import java.util.List;

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

    public T getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public Integer getInt() {
        return (Integer) value;
    }

    public Double getDouble() {
        return (Double) value;
    }

    public Boolean getBoolean() {
        return (Boolean) value;
    }

    public String getString() {
        return (String) value;
    }

    public List<String> getStringList() {
        return (List<String>) value;
    }

    @Override
    public String toString() {
        return getString();
    }
}