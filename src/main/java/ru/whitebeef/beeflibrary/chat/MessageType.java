package ru.whitebeef.beeflibrary.chat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class MessageType {

    private static final Map<String, String> messages = new HashMap<>();

    public static void registerTypes(Plugin instance) {
        FileConfiguration cfg = instance.getConfig();
        String prefix = instance.getName().toLowerCase() + ".";
        for (String type : cfg.getConfigurationSection("messages").getKeys(false)) {
            messages.put(prefix + type, cfg.getString("messages." + type));
        }
    }

    public static void unregisterTypes(Plugin instance) {
        FileConfiguration cfg = instance.getConfig();
        String prefix = instance.getName().toLowerCase() + ".";
        for (String type : cfg.getConfigurationSection("messages").getKeys(false)) {
            messages.remove(prefix + type);
        }
    }

    @NotNull
    public static String of(@NotNull String type) {
        return messages.getOrDefault(type, "");
    }

    public static String of(@NotNull Plugin plugin, String type) {
        return messages.getOrDefault(plugin.getName().toLowerCase() + "." + type, "");
    }

}
