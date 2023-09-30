package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Player IP address aggregator.
 * <p>
 * Aggregates players by their IP addresses. Primary use is to determine
 * alt accounts online, also can be used to retrieve player's IP address
 * in multiserver environments (requires Jedis to be enabled).
 * <p>
 * Initially, this class was copied from {@link PlayerNameUtils}.
 * Reason to create this class is to replace poorly-documented
 * and buggy {@link PlayerInetUtils} (which is now obsolete).
 */
public final class PlayerAddressUtils {
    private static Implementation implementation;

    /**
     * Retrieves IP address of the player.
     *
     * @param player the player
     * @return address of the player
     */
    public static @Nullable String getAddress(Player player) {
        return implementation.getAddress(player);
    }

    /**
     * Retrieves IP address of the player.
     *
     * @param uniqueId UUID of the player
     * @return address of the player
     */
    public static @Nullable String getAddress(UUID uniqueId) {
        return implementation.getAddress(uniqueId);
    }

    /**
     * Retrieves all UUIDs that are logged in from the same IP address as the specified player.
     *
     * @param player the player
     * @return UUIDs of all players (including specified one)
     */
    public static @NotNull @UnmodifiableView Set<UUID> getAltAccounts(Player player) {
        return implementation.getAltAccounts(player);
    }

    /**
     * Retrieves all UUIDs that are logged in from the same IP address as the specified player.
     *
     * @param uniqueId UUID of the player
     * @return UUIDs of all players (including specified one)
     */
    public static @NotNull @UnmodifiableView Set<UUID> getAltAccounts(UUID uniqueId) {
        return implementation.getAltAccounts(uniqueId);
    }

    /**
     * Retrieves all UUIDs that are logged in from the specified IP address.
     *
     * @param address IP address
     * @return UUIDs of all players
     */
    public static @NotNull @UnmodifiableView Set<UUID> getPlayers(String address) {
        return implementation.getPlayers(address);
    }

    /**
     * Initializes the implementation.
     * <strong>Shouldn't be called by plugins that depend on BeefLibrary.</strong>
     */
    @ApiStatus.Internal
    public static void init() {
        if (JedisUtils.isJedisEnabled()) {
            implementation = new JedisImplementation();
        } else {
            implementation = new InMemoryImplementation();
        }

        Bukkit.getOnlinePlayers().forEach(implementation::addPlayer);
    }

    /**
     * Returns implementation instance.
     * <strong>Shouldn't be called by plugins that depend on BeefLibrary.</strong>
     */
    @ApiStatus.Internal
    public static Implementation getImplementation() {
        return implementation;
    }

    @ApiStatus.Internal
    private PlayerAddressUtils() {
        throw new UnsupportedOperationException("This class cannot be instanced");
    }

    @ApiStatus.Internal
    public sealed interface Implementation permits InMemoryImplementation, JedisImplementation {

        default @Nullable String getAddress(Player player) {
            if (player.isOnline()) {
                return player.getAddress().getHostString();
            } else {
                return getAddress(player.getUniqueId());
            }
        }

        @Nullable String getAddress(UUID uniqueId);

        default @NotNull @UnmodifiableView Set<UUID> getAltAccounts(Player player) {
            return getPlayers(getAddress(player));
        }

        default @NotNull @UnmodifiableView Set<UUID> getAltAccounts(UUID uniqueId) {
            return getPlayers(getAddress(uniqueId));
        }

        @NotNull @UnmodifiableView Set<UUID> getPlayers(String address);

        void addPlayer(Player player);

        void removePlayer(Player player);

    }

    private static final class InMemoryImplementation implements Implementation {
        private final Map<String, Set<UUID>> players = new HashMap<>();
        private final Map<String, Set<UUID>> addressPlayers = new HashMap<>();
        private final Map<UUID, String> playerAddresses = new HashMap<>();

        @Override
        public String getAddress(Player player) {
            // forcibly use the map, it's cheaper
            return playerAddresses.get(player.getUniqueId());
        }

        @Override
        public String getAddress(UUID uniqueId) {
            return playerAddresses.get(uniqueId);
        }

        @Override
        public @NotNull @UnmodifiableView Set<UUID> getPlayers(String address) {
            Set<UUID> uuids = addressPlayers.get(address);
            return uuids == null ? Collections.emptySet() : Collections.unmodifiableSet(uuids);
        }

        @Override
        public void addPlayer(Player player) {
            if (!player.isOnline()) {
                throw new IllegalStateException("Player is offline");
            }

            String address = player.getAddress().getHostString();
            playerAddresses.put(player.getUniqueId(), address);
            addressPlayers.compute(address, (k, v) -> {
                if (v == null) return new HashSet<>();
                v.add(player.getUniqueId());
                return v;
            });
        }

        @Override
        public void removePlayer(Player player) {
            String address = playerAddresses.remove(player.getUniqueId());
            if (address == null) return;

            players.computeIfPresent(address, (k, v) -> {
                v.remove(player.getUniqueId());
                return v.isEmpty() ? null : v;
            });
        }
    }

    /**
     * Implementation note:
     * I don't get wtf is this so kept it as is.
     * If you really want to rewrite this,
     * <s>kick WhiteBeef in his butt</s> ask WhiteBeef for help.
     */
    private static final class JedisImplementation implements Implementation {
        private static final String ADDRESS_KEY_PREFIX = "IP:";
        private static final String PLAYER_KEY_PREFIX = "cacheIPs:";

        @Override
        public @Nullable String getAddress(Player player) {
            return getIP(player);
        }

        @Override
        public @Nullable String getAddress(UUID uniqueId) {
            return JedisUtils.jedisGet(BeefLibrary.getInstance(), PLAYER_KEY_PREFIX.concat(uniqueId.toString()));
        }

        @Override
        public @NotNull @UnmodifiableView Set<UUID> getPlayers(String address) {
            return JedisUtils.jedisGetSet(BeefLibrary.getInstance(), ADDRESS_KEY_PREFIX.concat(address))
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());
        }

        private String getIP(Player player) {
            InetSocketAddress socketAddress = player.getAddress();
            if (socketAddress == null) {
                throw new IllegalStateException("Player with name " + player.getName() + " has not inet socket address");
            }
            JedisUtils.jedisSet(BeefLibrary.getInstance(), PLAYER_KEY_PREFIX + player.getUniqueId(), socketAddress.getHostString());
            return socketAddress.getHostString();
        }

        @Override
        public void addPlayer(Player player) {
            String ip = getIP(player);

            JedisUtils.jedisAddInSet(BeefLibrary.getInstance(), ADDRESS_KEY_PREFIX + ip, player.getUniqueId().toString());
        }

        @Override
        public void removePlayer(Player player) {
            String ip = getIP(player);

            Set<String> uuids = JedisUtils.jedisGetSet(BeefLibrary.getInstance(), ADDRESS_KEY_PREFIX + ip);

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
