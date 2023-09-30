package ru.whitebeef.beeflibrary.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Obsolete(since = "2.2.0")
public class PlayerInetUtils {
    private static final PlayerInetUtils INSTANCE = new PlayerInetUtils();

    public static PlayerInetUtils getInstance() {
        return INSTANCE;
    }

    @ApiStatus.Obsolete(since = "2.2.0")
    public static String getIP(Player player) {
        if (player.getAddress() == null) {
            throw new IllegalStateException("Player with name " + player.getName() + " has not inet socket address");
        }
        return PlayerAddressUtils.getAddress(player);
    }

    @ApiStatus.Obsolete(since = "2.2.0")
    @Nullable
    public static String getIP(UUID playerUuid) {
        return PlayerAddressUtils.getAddress(playerUuid);
    }

    @ApiStatus.Obsolete(since = "2.2.0")
    public static Set<UUID> getPlayersWithSimilarIp(Player player) {
        return new HashSet<>(PlayerAddressUtils.getAltAccounts(player));
    }

    @ApiStatus.Obsolete(since = "2.2.0")
    public static Set<UUID> getPlayersWithSimilarIp(UUID playerUuid) {
        return new HashSet<>(PlayerAddressUtils.getAltAccounts(playerUuid));
    }

    private PlayerInetUtils() {
    }

    @Deprecated(forRemoval = true, since = "2.2.0")
    public void addPlayer(Player player) {
        PlayerAddressUtils.getImplementation().addPlayer(player);
    }

    @Deprecated(forRemoval = true, since = "2.2.0")
    public void removePlayer(Player player) {
        PlayerAddressUtils.getImplementation().removePlayer(player);
    }
}
