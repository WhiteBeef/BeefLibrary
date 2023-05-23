package ru.whitebeef.beeflibrary.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Cooldown {

    private final Type type;
    private final long start;
    private final long value;

    public static Cooldown of(Type type, long value) {
        return new Cooldown(type, value);
    }

    public static Cooldown of(long value) {
        return new Cooldown(value);
    }

    public Cooldown(Type type, long value) {
        this.type = type;
        this.value = value;
        switch (type) {
            case TICKS -> start = Bukkit.getCurrentTick();
            case MILLIS -> start = System.currentTimeMillis();
            default -> start = System.currentTimeMillis();
        }
    }

    public Cooldown(long millis) {
        this(Type.MILLIS, millis);
    }

    public Type getType() {
        return type;
    }

    public long getValue() {
        return value;
    }

    public boolean isCooldownPassed() {
        return switch (type) {
            case TICKS -> Bukkit.getCurrentTick() - start > value;
            case MILLIS -> System.currentTimeMillis() - start > value;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cooldown cooldown = (Cooldown) o;
        return start == cooldown.start && value == cooldown.value && type == cooldown.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, start, value);
    }

    public boolean isCooldownPassed(@NotNull CommandSender sender, @NotNull String skipPermission) {
        return (!skipPermission.isEmpty() && sender.hasPermission(skipPermission)) || switch (type) {
            case TICKS -> Bukkit.getCurrentTick() - start > value;
            case MILLIS -> System.currentTimeMillis() - start > value;
        };
    }

    public enum Type {
        TICKS,
        MILLIS;
    }
}
