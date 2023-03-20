package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;

public class ScheduleUtils {

    public static void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(BeefLibrary.getInstance(), runnable);
    }

    public static void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BeefLibrary.getInstance(), runnable);
    }

    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void scheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
    }

    public static void scheduleAsyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, runnable, delay, period);
    }

}
