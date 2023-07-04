package ru.whitebeef.beeflibrary.entites;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.database.LazyPlayerDatabase;
import ru.whitebeef.beeflibrary.utils.GsonUtils;
import ru.whitebeef.beeflibrary.utils.JedisUtils;
import ru.whitebeef.beeflibrary.utils.ScheduleUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class LazyPlayer {

    private static final Map<String, Pair<Class<? extends LazyPlayer>, Class<? extends LazyPlayerData>>> registeredTypes = new HashMap<>();
    private static final Map<String, Map<UUID, LazyPlayer>> loadedPlayers = new HashMap<>();
    private static final Map<String, Set<LazyPlayer>> toSave = new HashMap<>();

    private static BukkitRunnable lazyBukkitSaveTask = null;
    private static ScheduledTask lazyScheduledSaveTask = null;

    public static void registerLazyPlayerType(@NotNull Plugin plugin, @NotNull Class<? extends LazyPlayer> lazyPlayerClass,
                                              @NotNull Class<? extends LazyPlayerData> lazyPlayerDataClass) {
        registeredTypes.put(plugin.getName(), Pair.of(lazyPlayerClass, lazyPlayerDataClass));
    }

    @Nullable
    public static Pair<Class<? extends LazyPlayer>, Class<? extends LazyPlayerData>> getRegisteredTypes(Plugin plugin) {
        return registeredTypes.get(plugin.getName());
    }

    public static void startLazySaveTask() {

        if (BeefLibrary.getInstance().isFolia()) {
            lazyScheduledSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(BeefLibrary.getInstance(), (scheduledTask) -> LazyPlayer.saveAll(),
                    25, 25, TimeUnit.SECONDS);
        } else {
            lazyBukkitSaveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    LazyPlayer.saveAll();
                }
            };

            lazyBukkitSaveTask.runTaskTimerAsynchronously(BeefLibrary.getInstance(), 500L, 500L);
        }
    }

    public static void stopLazySaveTask() {
        if (lazyBukkitSaveTask != null) {
            lazyBukkitSaveTask.cancel();
        }
        if (lazyScheduledSaveTask != null) {
            lazyScheduledSaveTask.cancel();
        }
    }

    public static void lazyLoad(Plugin plugin, Player player) {
        ScheduleUtils.runTaskAsynchronously(() -> LazyPlayer.of(plugin, player));
    }

    public static void saveAll() {
        for (var toSave : toSave.values()) {
            toSave.removeIf(LazyPlayer::save);
        }
    }

    private static void addCache(@NotNull Plugin plugin, @NotNull LazyPlayer lazyPlayer) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedPlayers.computeIfAbsent(lazyPlayer.getPluginName(), k -> new HashMap<>()).put(lazyPlayer.getPlayerUuid(), lazyPlayer);
            return;
        }
        JedisUtils.jedisSet(plugin, lazyPlayer.getPlayerUuid().toString(), GsonUtils.parseObject(lazyPlayer.getData()));
    }

    @Nullable
    private static LazyPlayer getCache(@NotNull Plugin plugin, @NotNull Player player) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedPlayers.getOrDefault(plugin.getName(), new HashMap<>()).get(player.getUniqueId());
        }
        String json = JedisUtils.jedisGet(plugin, player.getUniqueId().toString());
        if (json == null) {
            return null;
        }
        var pair = registeredTypes.get(plugin.getName());
        try {
            return pair.left().getDeclaredConstructor(Plugin.class, Player.class, pair.right())
                    .newInstance(plugin, player, GsonUtils.parseJSON(json, pair.right()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean inCache(Plugin plugin, Player player) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedPlayers.getOrDefault(plugin.getName(), new HashMap<>()).get(player.getUniqueId()) != null;
        }
        String json = JedisUtils.jedisGet(plugin, player.getUniqueId().toString());
        return json != null;
    }

    private static void removeCache(@NotNull Plugin plugin, @NotNull Player player) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedPlayers.getOrDefault(plugin.getName(), new HashMap<>()).remove(player.getUniqueId());
            return;
        }
        JedisUtils.jedisDel(plugin, player.getUniqueId().toString());
    }

    public static void unload(Plugin plugin, Player player) {
        LazyPlayer lazyPlayer = getCache(plugin, player);
        if (lazyPlayer != null) {
            lazyPlayer.save();
        }
        removeCache(plugin, player);
    }

    public static LazyPlayer of(Plugin plugin, Player player) {
        LazyPlayer lazyPlayer = getCache(plugin, player);
        if (lazyPlayer != null) {
            return lazyPlayer;
        }
        lazyPlayer = LazyPlayerDatabase.getInstance().getLazyPlayer(plugin, player);

        if (lazyPlayer != null) {
            addCache(plugin, lazyPlayer);
            return lazyPlayer;
        }

        var pair = registeredTypes.get(plugin.getName());
        try {
            lazyPlayer = pair.left().getDeclaredConstructor(Plugin.class, Player.class)
                    .newInstance(plugin, player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        addCache(plugin, lazyPlayer);

        return lazyPlayer;
    }


    private final String pluginName;
    private final UUID playerUuid;
    private final LazyPlayerData data;

    public LazyPlayer(Plugin plugin, Player player, LazyPlayerData data) {
        this.pluginName = plugin.getName();
        this.playerUuid = player.getUniqueId();
        this.data = data;
    }

    public LazyPlayer(Plugin plugin, Player player) {
        this.pluginName = plugin.getName();
        this.playerUuid = player.getUniqueId();
        this.data = getDefaultData();
    }

    protected abstract LazyPlayerData getDefaultData();

    public String getPluginName() {
        return pluginName;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public LazyPlayerData getData() {
        return data;
    }

    public void lazySave(Plugin plugin) {
        addCache(plugin, this);
        toSave.computeIfAbsent(pluginName, k -> new HashSet<>()).add(this);
    }

    public boolean save() {
        return LazyPlayerDatabase.getInstance().saveRewardPlayer(this);
    }
}
