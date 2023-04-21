package ru.whitebeef.beeflibrary.utils;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitUtils {

    public static List<String> getOnlinePlayerNames() {
        if (MultiLib.isMultiPaper()) {
            return Bukkit.getAllOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
