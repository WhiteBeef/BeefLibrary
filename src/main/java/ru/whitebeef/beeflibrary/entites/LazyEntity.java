package ru.whitebeef.beeflibrary.entites;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.database.Column;
import ru.whitebeef.beeflibrary.database.LazyEntityDatabase;
import ru.whitebeef.beeflibrary.database.Table;
import ru.whitebeef.beeflibrary.database.abstractions.Database;
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
    private static final Map<String, Map<Class<? extends LazyEntity>, Class<? extends LazyEntityData>>> registeredTypes = new HashMap<>();
    private static final Map<String, Map<Class<? extends LazyEntity>, String>> registeredLazyDatabaseNames = new HashMap<>();

    private static final Map<String, Map<UUID, Map<Class<? extends LazyEntity>, LazyEntity>>> loadedEntities = new HashMap<>();
    private static final Map<String, Set<LazyEntity>> toSave = new HashMap<>();

    private static BukkitTask lazyBukkitSaveTask = null;
    private static ScheduledTask lazyScheduledSaveTask = null;

    public static void registerLazyEntityType(@NotNull Plugin plugin, @NotNull Class<? extends LazyEntity> lazyEntityClass,
                                              @NotNull Class<? extends LazyEntityData> lazyEntityDataClass, String databasePath) {
        Database database = new LazyEntityDatabase(plugin, databasePath, lazyEntityClass, lazyEntityDataClass)
                .addTable(new Table("LazyEntities")
                        .addColumn(new Column("uuid", "VARCHAR(65) PRIMARY KEY"))
                        .addColumn(new Column("data", "TEXT")));
        database.setup();
        registeredTypes.computeIfAbsent(plugin.getName(), k -> new HashMap<>()).put(lazyEntityClass, lazyEntityDataClass);
        registeredLazyDatabaseNames.computeIfAbsent(plugin.getName(), k -> new HashMap<>()).put(lazyEntityClass, database.getDatabaseName());
    }

    public static Set<String> getRegisteredPluginNames() {
        return registeredTypes.keySet();
    }

    @NotNull
    public static Map<Class<? extends LazyEntity>, Class<? extends LazyEntityData>> getRegisteredTypes(Plugin plugin) {
        return registeredTypes.getOrDefault(plugin.getName(), new HashMap<>());
    }

    public static void startLazySaveTask() {

        if (BeefLibrary.getInstance().isFolia()) {
            lazyScheduledSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(BeefLibrary.getInstance(),
                    (scheduledTask) -> LazyEntity.saveAll(),
                    60, 60, TimeUnit.SECONDS);
        } else {
            lazyBukkitSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(BeefLibrary.getInstance(), LazyEntity::saveAll, 1200L, 1200L);
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
        getRegisteredTypes(plugin).forEach((key, value) ->
                ScheduleUtils.runTaskAsynchronously(() -> LazyEntity.of(plugin, entityUuid, key)));
    }

    public static void saveAll() {
        for (var entry : toSave.entrySet()) {
            Set<LazyEntity> unsaved = new HashSet<>(entry.getValue());
            unsaved.removeIf(LazyEntity::save);
            toSave.put(entry.getKey(), unsaved);
        }
    }

    private static void addCache(@NotNull LazyEntity lazyEntity) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedEntities.computeIfAbsent(lazyEntity.getPluginName(), k -> new HashMap<>())
                    .computeIfAbsent(lazyEntity.getEntityUuid(), k -> new HashMap<>())
                    .putIfAbsent(lazyEntity.getClass(), lazyEntity);
            return;
        }
        JedisUtils.getJedis().set(lazyEntity.getPluginName() + ":" + lazyEntity.getClass().getSimpleName() + ":" +
                lazyEntity.getEntityUuid().toString(), GsonUtils.parseObject(lazyEntity.getData()));
    }

    @Nullable
    private static LazyEntity getCache(@NotNull Plugin plugin, @NotNull UUID entityUuid, Class<? extends LazyEntity> lazyEntityClass) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).getOrDefault(entityUuid, new HashMap<>()).get(lazyEntityClass);
        }

        Class<? extends LazyEntityData> lazyEntityDataClass = registeredTypes.getOrDefault(plugin.getName(), new HashMap<>())
                .get(lazyEntityClass);
        if (lazyEntityDataClass == null) {
            return null;
        }

        String json = JedisUtils.jedisGet(plugin, lazyEntityClass.getSimpleName() + ":" + entityUuid);
        if (json == null) {
            return null;
        }
        try {
            return lazyEntityClass.getDeclaredConstructor(Plugin.class, UUID.class, lazyEntityDataClass)
                    .newInstance(plugin, entityUuid, GsonUtils.parseJSON(json, lazyEntityDataClass));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isInCache(Plugin plugin, UUID entityUuid, Class<? extends LazyEntity> lazyEntityClass) {
        if (!JedisUtils.isJedisEnabled()) {
            return loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).get(entityUuid) != null;
        }

        String json = JedisUtils.jedisGet(plugin, lazyEntityClass.getSimpleName() + ":" + entityUuid);
        return json != null;
    }

    private static void removeCache(@NotNull Plugin plugin, @NotNull UUID entityUuid, Class<? extends LazyEntity> lazyEntityClass) {
        if (!JedisUtils.isJedisEnabled()) {
            loadedEntities.getOrDefault(plugin.getName(), new HashMap<>()).remove(entityUuid);
            return;
        }

        JedisUtils.jedisDel(plugin, lazyEntityClass.getSimpleName() + ":" + entityUuid);
    }

    public static void unload(Plugin plugin, UUID entityUuid, Class<? extends LazyEntity> lazyEntityClass) {
        LazyEntity lazyEntity = getCache(plugin, entityUuid, lazyEntityClass);
        if (lazyEntity != null) {
            lazyEntity.save();
        }
        removeCache(plugin, entityUuid, lazyEntityClass);
    }

    public static void unloadAll(Plugin plugin, UUID entityUuid) {
        getRegisteredTypes(plugin).forEach((lazyEntityClass, lazyEntityDataClass) -> {
            LazyEntity lazyEntity = getCache(plugin, entityUuid, lazyEntityClass);
            if (lazyEntity != null) {
                lazyEntity.save();
            }
            removeCache(plugin, entityUuid, lazyEntityClass);
        });
    }

    public static LazyEntity of(Plugin plugin, UUID entityUuid,
                                Class<? extends LazyEntity> lazyEntityClass) {
        LazyEntity lazyEntity = getCache(plugin, entityUuid, lazyEntityClass);
        if (lazyEntity != null) {
            return lazyEntity;
        }
        String databaseName = registeredLazyDatabaseNames.getOrDefault(plugin.getName(), new HashMap<>()).get(lazyEntityClass);
        if (databaseName == null) {
            throw new RuntimeException("Unable to found LazyEntity database");
        }
        lazyEntity = ((LazyEntityDatabase) Database.getDatabase(plugin, databaseName)).getLazyEntity(plugin, entityUuid);
        if (lazyEntity != null) {
            addCache(lazyEntity);
            return lazyEntity;
        }

        try {
            lazyEntity = lazyEntityClass.getDeclaredConstructor(Plugin.class, UUID.class)
                    .newInstance(plugin, entityUuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addCache(lazyEntity);
        lazyEntity.lazySave();
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

    public void lazySave() {
        addCache(this);
        toSave.computeIfAbsent(pluginName, k -> new HashSet<>()).add(this);
    }

    public boolean save() {
        String databaseName = registeredLazyDatabaseNames.getOrDefault(getPluginName(), new HashMap<>()).get(super.getClass());
        if (databaseName == null) {
            throw new RuntimeException("Unable to found LazyEntity database");
        }
        return ((LazyEntityDatabase) Database.getDatabase(pluginName, databaseName)).saveLazyEntity(this);
    }

    @Override
    public String toString() {
        return "LazyEntity{" +
                "pluginName='" + pluginName + '\'' +
                ", entityUuid=" + entityUuid +
                ", data=" + GsonUtils.parseObject(data) +
                '}';
    }
}
