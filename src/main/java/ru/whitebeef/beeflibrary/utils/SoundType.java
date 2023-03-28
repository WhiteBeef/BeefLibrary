package ru.whitebeef.beeflibrary.utils;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SoundType {

    private static final HashMap<String, Sound> loadedSounds = new HashMap<>();

    public static void addNewSound(String namespace, Sound sound) {
        loadedSounds.put(namespace, sound);
    }

    private static void loadSounds(Plugin plugin, ConfigurationSection section) {
        if (section == null) {
            return;
        }
        for (String soundNamespace : section.getKeys(false)) {
            if (!section.isConfigurationSection(soundNamespace) || !section.isString(soundNamespace + ".sound")
                    || !section.isDouble(soundNamespace + ".volume") || !section.isDouble(soundNamespace + ".pitch")) {
                continue;
            }

            ConfigurationSection soundSection = section.getConfigurationSection(soundNamespace);
            addNewSound(soundNamespace, Sound.sound(Key.key(soundSection.getString("sound").toLowerCase()), Sound.Source.AMBIENT, (float)
                    soundSection.getDouble("volume"), (float) soundSection.getDouble("pitch")));
        }
    }

    public static void registerTypesSection(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isConfigurationSection(path)) {
            return;
        }
        loadSounds(instance, cfg.getConfigurationSection(path));
    }

    public static void unregisterTypesSection(Plugin instance) {
        String prefix = instance.getName().toLowerCase() + ".";
        loadedSounds.entrySet()
                .removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    public static void registerType(Plugin instance, String path) {
        loadSounds(instance, instance.getConfig().getConfigurationSection(instance.getName().toLowerCase() + "." + path));
    }

    public static void registerType(String path, Sound value) {
        if (loadedSounds.containsKey(path)) {
            throw new IllegalArgumentException("Path '" + path + "' is already registered!");
        }

        loadedSounds.put(path, value);
    }

    public static void unregisterType(String path) {
        if (loadedSounds.containsKey(path)) {
            return;
        }

        loadedSounds.remove(path);
    }

    public static void unregisterType(Plugin instance, String path) {
        FileConfiguration cfg = instance.getConfig();
        if (!cfg.isString(path)) {
            return;
        }

        loadedSounds.remove(instance.getName().toLowerCase() + "." + path, cfg.getString(path));
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

    @Nullable
    public static Sound of(@NotNull String type) {
        return loadedSounds.get(type);
    }

    public static Sound of(@NotNull Plugin plugin, String type) {
        return loadedSounds.get(plugin.getName().toLowerCase() + "." + type);
    }

    public static void play(Player player, String namespace) {
        Sound sound = loadedSounds.get(namespace);
        if (sound == null) {
            return;
        }
        player.playSound(sound);
    }

    public static void play(Plugin plugin, Player player, String namespace) {
        Sound sound = loadedSounds.get(plugin.getName().toLowerCase() + "." + namespace);
        if (sound == null) {
            return;
        }
        player.playSound(sound);
    }

}
