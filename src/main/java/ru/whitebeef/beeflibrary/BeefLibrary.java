package ru.whitebeef.beeflibrary;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;

public final class BeefLibrary extends JavaPlugin {

    private static BeefLibrary instance;
    private boolean placeholderAPIHooked = false;

    public static BeefLibrary getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onDisable() {
        AbstractCommand.unregisterAllCommands();
    }

    public void tryHookPlaceholderAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIHooked = true;
            getLogger().info("PlaceholderAPI hooked!");
        }
    }

    public void unhookPlaceholderAPI() {
        placeholderAPIHooked = false;
        getLogger().info("PlaceholderAPI unhooked!");
    }

    public boolean isPlaceholderAPIHooked() {
        return placeholderAPIHooked;
    }

    public static void registerListeners(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }
}
