package ru.whitebeef.beeflibrary.utils;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
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
            Bukkit.getAsyncScheduler().runDelayed(plugin, (scheduledTask) -> runnable.run(), delay * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }

    public static void scheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (scheduledTask) -> runnable.run(), delay, period);
        } else {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
        }
    }

    public static void scheduleAsyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (BeefLibrary.getInstance().isFolia()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (scheduledTask) -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
        }
    }

    public static void scheduleAsyncRepeatingTaskTimes(Plugin plugin, Runnable runnable, long period, int times) {
        if (BeefLibrary.getInstance().isFolia()) {
            ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> runnable.run(), 0, period * 50, TimeUnit.MILLISECONDS);
            runTaskLaterAsynchronously(plugin, scheduledTask::cancel, times * period);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0, period);
            runTaskLaterAsynchronously(plugin, bukkitTask::cancel, times * period);
        }
    }

}
