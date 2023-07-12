package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
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
    private final Map<UUID, String> cacheUuids = new HashMap<>();

    public PlayerInetUtils() {
        instance = this;

        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
    }

    public static String getIP(Player player) {
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress == null) {
            throw new IllegalStateException("Player with name " + player.getName() + " has not inet socket address");
        }
        if (!JedisUtils.isJedisEnabled()) {
            instance.cacheUuids.put(player.getUniqueId(), socketAddress.getHostString());
        } else {
            JedisUtils.jedisSet(BeefLibrary.getInstance(), "cacheIPs:" + player.getUniqueId(), socketAddress.getHostString());
        }
        return socketAddress.getHostString();
    }

    @Nullable
    public static String getIP(UUID playerUuid) {
        if (!JedisUtils.isJedisEnabled()) {
            return instance.cacheUuids.get(playerUuid);
        } else {
            return JedisUtils.jedisGet(BeefLibrary.getInstance(), "cacheIPs:" + playerUuid);
        }
    }

    public static Set<UUID> getPlayersWithSimilarIp(Player player) {
        String ip = getIP(player);

        if (!JedisUtils.isJedisEnabled()) {
            return getInstance().players.getOrDefault(ip, new HashSet<>());
        } else {
            return JedisUtils.jedisGetSet(BeefLibrary.getInstance(), "IP:" + ip).stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
    }

    public static Set<UUID> getPlayersWithSimilarIp(UUID playerUuid) {
        String ip = getIP(playerUuid);
        if (ip == null) {
            return new HashSet<>();
        }

        if (!JedisUtils.isJedisEnabled()) {
            return getInstance().players.getOrDefault(ip, new HashSet<>());
        } else {
            return JedisUtils.jedisGetSet(BeefLibrary.getInstance(), "IP:" + ip).stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        }
    }

    public void addPlayer(Player player) {
        String ip = getIP(player);

        if (!JedisUtils.isJedisEnabled()) {
            players.computeIfAbsent(ip, k -> new HashSet<>()).add(player.getUniqueId());
        } else {
            JedisUtils.jedisAddInSet(BeefLibrary.getInstance(), "IP:" + ip, player.getUniqueId().toString());
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
            Set<String> uuids = JedisUtils.jedisGetSet(BeefLibrary.getInstance(), "IP:" + ip);

            if (uuids.isEmpty()) {
                return;
            }
            uuids.remove(player.getUniqueId().toString());

            if (uuids.isEmpty()) {
                JedisUtils.jedisDel(BeefLibrary.getInstance(), ip);
            } else {
                JedisUtils.jedisSetSet(BeefLibrary.getInstance(), ip, uuids);
            }
        }

    }

}
