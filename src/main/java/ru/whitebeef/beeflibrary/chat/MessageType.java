package ru.whitebeef.beeflibrary.chat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class MessageType {

    private static final Map<String, String> messages = new HashMap<>();

    public static void registerTypesSection(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isConfigurationSection(path)) {
            return;
        }
        String prefix = instance.getName().toLowerCase() + ".";
        for (String type : cfg.getConfigurationSection(path).getKeys(false)) {
            messages.put(prefix + type, cfg.getString(path + "." + type));
        }
    }

    public static void unregisterTypesSection(Plugin instance) {
        String prefix = instance.getName().toLowerCase() + ".";
        messages.entrySet()
                .removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    public static void registerType(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isString(path)) {
            return;
        }

        messages.put(instance.getName().toLowerCase() + "." + path, cfg.getString(path));
    }

    public static void registerType(String path, String value) {
        if (messages.containsKey(path)) {
            throw new IllegalArgumentException("Path '" + path + "' is already registered!");
        }

        messages.put(path, value);
    }

    public static void unregisterType(String path) {
        if (messages.containsKey(path)) {
            return;
        }

        messages.remove(path);
    }

    public static void unregisterType(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isString(path)) {
            return;
        }

        messages.remove(instance.getName().toLowerCase() + "." + path, cfg.getString(path));
    }

    public static void registerTypes(Plugin instance, String... paths) {
        for (String path : paths) {
            registerType(instance, path);
        }
    }

    public static void unregisterTypes(String... paths) {
        for (String path : paths) {
            unregisterType(path);
        }
    }

    public static void unregisterTypes(Plugin instance, String... paths) {
        for (String path : paths) {
            unregisterType(instance, path);
        }
    }

    @NotNull
    public static String of(@NotNull String type) {
        return messages.getOrDefault(type, type);
    }

    public static String of(@NotNull Plugin plugin, String type) {
        return messages.getOrDefault(plugin.getName().toLowerCase() + "." + type, plugin.getName().toLowerCase() + "." + type);
    }

}
