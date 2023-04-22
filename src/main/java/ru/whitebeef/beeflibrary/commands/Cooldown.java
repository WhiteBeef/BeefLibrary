package ru.whitebeef.beeflibrary.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Cooldown {

    private final Type type;
    private final long start;
    private final long value;

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
            case TICKS -> Bukkit.getCurrentTick() - start > 0;
            case MILLIS -> System.currentTimeMillis() - start > 0;
        };
    }

    public boolean isCooldownPassed(@NotNull CommandSender sender, @NotNull String skipPermission) {
        return (!skipPermission.isEmpty() && sender.hasPermission(skipPermission)) || switch (type) {
            case TICKS -> Bukkit.getCurrentTick() - start > 0;
            case MILLIS -> System.currentTimeMillis() - start > 0;
        };
    }

    enum Type {
        TICKS,
        MILLIS;
    }
}
