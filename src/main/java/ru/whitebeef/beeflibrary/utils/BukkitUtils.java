package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitUtils {

    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
