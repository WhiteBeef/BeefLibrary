package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.concurrent.TimeUnit;

public class ScheduleUtils {

    public static void runTask(Runnable runnable) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(BeefLibrary.getInstance(), runnable);
        } else {
            Bukkit.getScheduler().runTask(BeefLibrary.getInstance(), runnable);
        }
    }

    public static void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (scheduledTask) -> runnable.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getAsyncScheduler().runNow(BeefLibrary.getInstance(), (scheduledTask) -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(BeefLibrary.getInstance(), runnable);
        }
    }

    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getAsyncScheduler().runDelayed(BeefLibrary.getInstance(), (scheduledTask) -> runnable.run(), delay * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }

    public static void scheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(BeefLibrary.getInstance(), (scheduledTask) -> runnable.run(), delay, period);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
    }

    public static void scheduleAsyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(BeefLibrary.getInstance(), (scheduledTask) -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, runnable, delay, period);
        }
    }

}
