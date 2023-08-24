package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarUtils implements Listener {

    private static BossBarUtils instance;

    public static BossBarUtils getInstance() {
        return instance;
    }

    private final HashMap<UUID, BossBar> showedBossBars = new HashMap<>();
    private final Map<UUID, Long> toRemove = new HashMap<>();

    public BossBarUtils() {
        instance = this;

        ScheduleUtils.scheduleSyncRepeatingTask(BeefLibrary.getInstance(), () -> {
            new HashMap<>(toRemove).forEach((uuid, timeToRemove) -> {
                if (timeToRemove > System.currentTimeMillis()) {
                    return;
                }
                if (!showedBossBars.containsKey(uuid)) {
                    return;
                }
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    toRemove.remove(uuid);
                    return;
                }
                player.hideBossBar(showedBossBars.get(uuid));
                toRemove.remove(uuid);
                showedBossBars.remove(uuid);
            });
        }, 20L, 20L);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        showedBossBars.remove(event.getPlayer().getUniqueId());
    }

    public void sendBossBar(Player player, BossBar bossBar) {
        if (showedBossBars.containsKey(player.getUniqueId())) {
            player.hideBossBar(showedBossBars.get(player.getUniqueId()));
        }
        player.showBossBar(bossBar);
        showedBossBars.put(player.getUniqueId(), bossBar);
    }

    public void sendBossBar(Player player, BossBar bossBar, long millis) {
        BossBar showedBossBar = showedBossBars.get(player.getUniqueId());
        if (showedBossBar != null) {
            if (showedBossBar == bossBar) {
                toRemove.put(player.getUniqueId(), System.currentTimeMillis() + millis);
                return;
            }
            player.hideBossBar(showedBossBar);
        }
        player.showBossBar(bossBar);

        showedBossBars.put(player.getUniqueId(), bossBar);
        toRemove.put(player.getUniqueId(), System.currentTimeMillis() + millis);
    }

    @Nullable
    public BossBar getLastBossBar(Player player) {
        return showedBossBars.get(player.getUniqueId());
    }

    public void hideLastBossBar(Player player) {
        if (!showedBossBars.containsKey(player.getUniqueId())) {
            return;
        }
        player.hideBossBar(showedBossBars.get(player.getUniqueId()));
    }

}
