package ru.whitebeef.beeflibrary.entites;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.database.LazyEntityDatabase;
import ru.whitebeef.beeflibrary.utils.GsonUtils;
import ru.whitebeef.beeflibrary.utils.JedisUtils;
import ru.whitebeef.beeflibrary.utils.ScheduleUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class LazyEntity {
    private static final Map<String, Pair<Class<? extends LazyEntity>, Class<? extends LazyEntityData>>> registeredTypes = new HashMap<>();
    private static final Map<String, Map<UUID, LazyEntity>> loadedEntities = new HashMap<>();
    private static final Map<String, Set<LazyEntity>> toSave = new HashMap<>();

    private static BukkitRunnable lazyBukkitSaveTask = null;
    private static ScheduledTask lazyScheduledSaveTask = null;

    public static void registerLazyEntityType(@NotNull Plugin plugin, @NotNull Class<? extends LazyEntity> lazyEntityClass,
                                              @NotNull Class<? extends LazyEntityData> lazyEntityDataClass) {
        registeredTypes.put(plugin.getName(), Pair.of(lazyEntityClass, lazyEntityDataClass));
    }

    public static Set<String> getRegisteredPluginNames() {
        return registeredTypes.keySet();
    }

    @Nullable
    public static Pair<Class<? extends LazyEntity>, Class<? extends LazyEntityData>> getRegisteredTypes(Plugin plugin) {
        return registeredTypes.get(plugin.getName());
    }

    public static void startLazySaveTask() {

        if (BeefLibrary.getInstance().isFolia()) {
            lazyScheduledSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(BeefLibrary.getInstance(), (scheduledTask) -> LazyEntity.saveAll(),
                    25, 25, TimeUnit.SECONDS);
        } else {
            lazyBukkitSaveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    LazyEntity.saveAll();
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

    public static void lazyLoad(Plugin plugin, UUID entityUuid) {
        ScheduleUtils.runTaskAsynchronously(() -> LazyEntity.of(plugin, entityUuid));
    }

    public static void saveAll() {
        for (var toSave : toSave.values()) {
            toSave.removeIf(LazyEntity::save);
        }
    }

    private static void addCache(@NotNull Plugin plugin, @NotNull LazyEntity lazyEntity) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedEntities.computeIfAbsent(lazyEntity.getPluginName(), k -> new HashMap<>()).put(lazyEntity.getEntityUuid(), lazyEntity);
            return;
        }
        JedisUtils.jedisSet(plugin, lazyEntity.getEntityUuid().toString(), GsonUtils.parseObject(lazyEntity.getData()));
    }

    @Nullable
    private static LazyEntity getCache(@NotNull Plugin plugin, @NotNull UUID entityUuid) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).get(entityUuid);
        }
        String json = JedisUtils.jedisGet(plugin, entityUuid.toString());
        if (json == null) {
            return null;
        }
        var pair = registeredTypes.get(plugin.getName());
        if (pair == null) {
            return null;
        }
        try {
            return pair.left().getDeclaredConstructor(Plugin.class, UUID.class, pair.right())
                    .newInstance(plugin, entityUuid, GsonUtils.parseJSON(json, pair.right()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInCache(Plugin plugin, UUID entityUuid) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).get(entityUuid) != null;
        }
        String json = JedisUtils.jedisGet(plugin, entityUuid.toString());
        return json != null;
    }

    private static void removeCache(@NotNull Plugin plugin, @NotNull UUID entityUuid) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).remove(entityUuid);
            return;
        }
        JedisUtils.jedisDel(plugin, entityUuid.toString());
    }

    public static void unload(Plugin plugin, UUID entityUuid) {
        LazyEntity lazyEntity = getCache(plugin, entityUuid);
        if (lazyEntity != null) {
            lazyEntity.save();
        }
        removeCache(plugin, entityUuid);
    }

    public static LazyEntity of(Plugin plugin, UUID entityUuid) {
        LazyEntity lazyEntity = getCache(plugin, entityUuid);
        if (lazyEntity != null) {
            return lazyEntity;
        }
        lazyEntity = LazyEntityDatabase.getInstance().getLazyEntity(plugin, entityUuid);

        if (lazyEntity != null) {
            addCache(plugin, lazyEntity);
            return lazyEntity;
        }

        var pair = registeredTypes.get(plugin.getName());
        try {
            lazyEntity = pair.left().getDeclaredConstructor(Plugin.class, UUID.class)
                    .newInstance(plugin, entityUuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addCache(plugin, lazyEntity);
        lazyEntity.lazySave(plugin);
        return lazyEntity;
    }


    private final String pluginName;
    private final UUID entityUuid;
    private final LazyEntityData data;

    public LazyEntity(Plugin plugin, UUID entityUuid, LazyEntityData data) {
        this.pluginName = plugin.getName();
        this.entityUuid = entityUuid;
        this.data = data;
    }

    public LazyEntity(Plugin plugin, UUID entityUuid) {
        this.pluginName = plugin.getName();
        this.entityUuid = entityUuid;
        this.data = getDefaultData();
    }

    protected abstract LazyEntityData getDefaultData();

    public String getPluginName() {
        return pluginName;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public LazyEntityData getData() {
        return data;
    }

    public void lazySave(Plugin plugin) {
        addCache(plugin, this);
        toSave.computeIfAbsent(pluginName, k -> new HashSet<>()).add(this);
    }

    public boolean save() {
        return LazyEntityDatabase.getInstance().saveLazyEntity(this);
    }
}
