package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;

public class PluginUtils {

    public static void enablePlugin(String name) {
        if ("BeefLibrary".equals(name)) {
            return;
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null) {
            return;
        }
        if (plugin.isEnabled()) {
            return;
        }

        for (String pluginName : plugin.getDescription().getDepend()) {
            enablePlugin(pluginName);
        }
        Bukkit.getPluginManager().enablePlugin(plugin);
    }
}
