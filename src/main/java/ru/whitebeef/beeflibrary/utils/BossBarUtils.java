package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.HashMap;
import java.util.UUID;

public class BossBarUtils implements Listener {

    private static BossBarUtils instance;

    public static BossBarUtils getInstance() {
        return instance;
    }

    private HashMap<UUID, BossBar> showedBossBars = new HashMap<>();

    public BossBarUtils() {
        instance = this;
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

    public void sendBossBar(Player player, BossBar bossBar, long timeTicks) {
        if (showedBossBars.containsKey(player.getUniqueId())) {
            player.hideBossBar(showedBossBars.get(player.getUniqueId()));
        }
        player.showBossBar(bossBar);

        showedBossBars.put(player.getUniqueId(), bossBar);
        ScheduleUtils.runTaskLater(BeefLibrary.getInstance(), () -> player.hideBossBar(bossBar), timeTicks);
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
