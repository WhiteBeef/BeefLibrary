package ru.whitebeef.beeflibrary.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.whitebeef.beeflibrary.entites.LazyEntity;
import ru.whitebeef.beeflibrary.utils.PlayerInetUtils;

public class PlayerJoinQuitHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        LazyEntity.getRegisteredPluginNames().forEach(pluginName ->
                LazyEntity.lazyLoad(Bukkit.getPluginManager().getPlugin(pluginName), event.getPlayer().getUniqueId()));
        PlayerInetUtils.getInstance().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        LazyEntity.getRegisteredPluginNames().forEach(pluginName ->
                LazyEntity.unload(Bukkit.getPluginManager().getPlugin(pluginName), event.getPlayer().getUniqueId()));

        PlayerInetUtils.getInstance().removePlayer(event.getPlayer());
    }

}
