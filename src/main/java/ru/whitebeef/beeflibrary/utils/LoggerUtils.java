package ru.whitebeef.beeflibrary.utils;

import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;

public class LoggerUtils {

    private static LoggerUtils instance;

    public static void info(Plugin plugin, String message) {
        plugin.getLogger().info(message);
    }

    public static void severe(Plugin plugin, String message) {
        plugin.getLogger().severe(message);
    }


    public static void debug(Plugin plugin, String message) {
          if (!BeefLibrary.getInstance().isDebug()) {
            return;
        }
        plugin.getLogger().info(message);
    }


}
