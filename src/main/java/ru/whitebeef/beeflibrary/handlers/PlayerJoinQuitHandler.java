package ru.whitebeef.beeflibrary.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.whitebeef.beeflibrary.utils.PlayerInetUtils;

public class PlayerJoinQuitHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerInetUtils.getInstance().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerInetUtils.getInstance().removePlayer(event.getPlayer());
    }

}
