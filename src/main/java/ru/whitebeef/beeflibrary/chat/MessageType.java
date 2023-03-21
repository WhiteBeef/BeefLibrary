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

    public static void unregisterTypesSection(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        String prefix = instance.getName().toLowerCase() + ".";
        if (!cfg.isConfigurationSection(path)) {
            return;
        }
        for (String type : cfg.getConfigurationSection(path).getKeys(false)) {
            messages.remove(prefix + type);
        }
    }

    public static void registerType(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isString(path)) {
            return;
        }

        messages.put(instance.getName().toLowerCase() + "." + path, cfg.getString(path));
    }

    public static void unregisterType(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isString(path)) {
            return;
        }

        messages.remove(instance.getName().toLowerCase() + "." + path, cfg.getString(path));
    }

    @NotNull
    public static String of(@NotNull String type) {
        return messages.getOrDefault(type, "");
    }

    public static String of(@NotNull Plugin plugin, String type) {
        return messages.getOrDefault(plugin.getName().toLowerCase() + "." + type, "");
    }

}
