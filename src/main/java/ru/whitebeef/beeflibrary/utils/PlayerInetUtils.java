package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerInetUtils {

    private static PlayerInetUtils instance;

    public static PlayerInetUtils getInstance() {
        return instance == null ? new PlayerInetUtils() : instance;
    }

    private final Map<String, Set<UUID>> players = new HashMap<>();

    public PlayerInetUtils() {
        instance = this;

        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
    }

    public static String getIP(Player player) {
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress == null) {
            throw new IllegalStateException("Player with name " + player.getName() + " has not inet socket address");
        }
        return socketAddress.getHostString();
    }

    public static Set<UUID> getPlayersWithSimilarIp(Player player) {
        return getInstance().players.getOrDefault(getIP(player), new HashSet<>());
    }

    public void addPlayer(Player player) {
        players.computeIfAbsent(getIP(player), k -> new HashSet<>()).add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        String ip = getIP(player);
        Set<UUID> uuids = players.get(ip);
        if (uuids == null) {
            return;
        }
        uuids.remove(player.getUniqueId());

        if (uuids.isEmpty()) {
            players.remove(ip);
        }
        players.put(ip, uuids);
    }

}
