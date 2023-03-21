package ru.whitebeef.beeflibrary;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.handlers.PluginHandler;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIHandler;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIManager;

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

        tryHookPlaceholderAPI();
        registerListeners(this, new PluginHandler(), new OldInventoryGUIHandler());
        new OldInventoryGUIManager();
    }

    @Override
    public void onDisable() {
        AbstractCommand.unregisterAllCommands(this);
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

    public void registerCommand(AbstractCommand abstractCommand) {
        abstractCommand.register(this);
    }

    public boolean registerPlaceholders(PlaceholderExpansion... expansions) {
        if (isPlaceholderAPIHooked()) {
            for (PlaceholderExpansion expansion : expansions) {
                expansion.register();
            }
        }
        return isPlaceholderAPIHooked();
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
