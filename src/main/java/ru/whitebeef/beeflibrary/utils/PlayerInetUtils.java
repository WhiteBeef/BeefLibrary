package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerInetUtils {

    private static PlayerInetUtils instance;

    public static PlayerInetUtils getInstance() {
        return instance;
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
        String ip = getIP(player);

        if (!JedisUtils.isJedisEnabled()) {
            return getInstance().players.getOrDefault(ip, new HashSet<>());
        } else {
            List<String> list = JedisUtils.jedisGetCollection(BeefLibrary.getInstance(), "ip:" + ip);
            return list == null ? new HashSet<>() : list.stream().map(UUID::fromString).collect(Collectors.toSet());
        }
    }

    public void addPlayer(Player player) {
        String ip = getIP(player);

        if (!JedisUtils.isJedisEnabled()) {
            players.computeIfAbsent(ip, k -> new HashSet<>()).add(player.getUniqueId());
        } else {
            JedisUtils.jedisAddInCollection(BeefLibrary.getInstance(), "ip:" + ip, player.getUniqueId().toString());
        }
    }

    public void removePlayer(Player player) {
        String ip = getIP(player);
        if (!JedisUtils.isJedisEnabled()) {
            Set<UUID> uuids = players.get(ip);
            if (uuids == null) {
                return;
            }
            uuids.remove(player.getUniqueId());

            if (uuids.isEmpty()) {
                players.remove(ip);
            }
            players.put(ip, uuids);
        } else {
            Set<String> uuids = JedisUtils.jedisGetCollection(BeefLibrary.getInstance(), "ip:" + ip).stream().collect(Collectors.toSet());

            if (uuids.isEmpty()) {
                return;
            }
            uuids.remove(player.getUniqueId().toString());

            if (uuids.isEmpty()) {
                JedisUtils.jedisDel(BeefLibrary.getInstance(), ip);
            }
            JedisUtils.jedisSetCollection(BeefLibrary.getInstance(), ip, uuids);
        }

    }

}
