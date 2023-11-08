package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Player uuid and name cache.
 * <p>
 * Technically this class implements vanilla Minecraft username.json file,
 * which is cache of uuids and names. It's used to fetch player's names and
 * uuids when they're offline. The main difference between vanilla cache is
 * that {@link PlayerNameUtils} is powered by Jedis, which allows its usage
 * in multiserver environments.
 * <p>
 * Initially, this class was copied from {@link PlayerInetUtils}
 */
public final class PlayerNameUtils {
    private static Implementation implementation;

    /**
     * Retrieves player's name by its UUID.
     *
     * @param uniqueId UUID of the player
     * @return name of the player or null if player wasn't stored
     */
    @Nullable
    public static String getName(UUID uniqueId) {
        return implementation.getName(uniqueId);
    }

    /**
     * Retrieves player's UUID by its name.
     *
     * @param name name of the player
     * @return UUID of the player or null if player wasn't stored
     */
    @Nullable
    public static UUID getUniqueId(String name) {
        return implementation.getUniqueId(name);
    }

    /**
     * Initializes the implementation.
     * <strong>Shouldn't be called by plugins that depend on BeefLibrary.</strong>
     */
    @Internal
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
    @Internal
    public static Implementation getImplementation() {
        return implementation;
    }

    @Internal
    private PlayerNameUtils() {
        throw new UnsupportedOperationException("This class cannot be instanced");
    }

    @Internal
    public sealed interface Implementation permits InMemoryImplementation, JedisImplementation {

        @Nullable
        String getName(UUID uniqueId);

        @Nullable
        UUID getUniqueId(String name);

        @NotNull
        Set<String> getPlayerNames();

        void addPlayer(Player player);

        void removePlayer(Player player);

    }

    private static final class InMemoryImplementation implements Implementation {
        private final Map<String, UUID> uuids = new HashMap<>();
        private final Map<UUID, String> names = new HashMap<>();

        @Override
        public @Nullable String getName(UUID uniqueId) {
            return names.get(uniqueId);
        }

        @Override
        public @Nullable UUID getUniqueId(String name) {
            return uuids.get(name);
        }

        @Override
        public @NotNull Set<String> getPlayerNames() {
            return uuids.keySet();
        }

        @Override
        public void addPlayer(Player player) {
            uuids.put(player.getName(), player.getUniqueId());
            names.put(player.getUniqueId(), player.getName());
        }

        @Override
        public void removePlayer(Player player) {
            uuids.remove(player.getName());
            names.remove(player.getUniqueId());
        }
    }

    private static final class JedisImplementation implements Implementation {

        @Override
        public @Nullable String getName(UUID uniqueId) {
            return JedisUtils.jedisGet(BeefLibrary.getInstance(), uniqueId.toString());
        }

        @Override
        public @Nullable UUID getUniqueId(String name) {
            String uuid = JedisUtils.jedisGet(BeefLibrary.getInstance(), name);
            return uuid == null ? null : UUID.fromString(uuid);
        }

        @Override
        public @NotNull Set<String> getPlayerNames() {
            return JedisUtils.jedisGetSet(BeefLibrary.getInstance(), "playerNames");
        }

        @Override
        public void addPlayer(Player player) {
            String uniqueId = player.getUniqueId().toString();
            String name = player.getName();

            JedisUtils.jedisSet(BeefLibrary.getInstance(), uniqueId, name);
            JedisUtils.jedisSet(BeefLibrary.getInstance(), name, uniqueId);
            JedisUtils.jedisAddInSet(BeefLibrary.getInstance(), "playerNames", name);
        }

        @Override
        public void removePlayer(Player player) {
            JedisUtils.jedisDel(BeefLibrary.getInstance(), player.getUniqueId().toString());
            JedisUtils.jedisDel(BeefLibrary.getInstance(), player.getName());
            JedisUtils.jedisRemoveFromSet(BeefLibrary.getInstance(), "playerNames", player.getName());
        }
    }
}
