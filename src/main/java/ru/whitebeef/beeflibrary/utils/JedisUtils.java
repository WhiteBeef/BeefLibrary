package ru.whitebeef.beeflibrary.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisUtils {

    private static Map<String, Map<String, JedisPubSub>> registeredPubSubs = new HashMap<>();

    private static JedisUtils instance;

    public static JedisUtils getInstance() {
        return instance;
    }

    private JedisPooled jedisSubscribe = null;
    private JedisPooled jedis = null;


    public JedisUtils() {
        instance = this;
        loadRedis();
    }


    private void loadRedis() {
        FileConfiguration config = BeefLibrary.getInstance().getConfig();
        if (config.getBoolean("redis.enable")) {
            jedis = new JedisPooled(config.getString("redis.host"), config.getInt("redis.port"), config.getString("redis.user"), config.getString("redis.password"));
        }
        if (config.getBoolean("redis.enable")) {
            jedisSubscribe = new JedisPooled(config.getString("redis.host"), config.getInt("redis.port"), config.getString("redis.user"), config.getString("redis.password"));
        }
    }

    public static boolean isJedisEnabled() {
        return instance.jedis != null;
    }

    public static JedisPooled getJedis() {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return instance.jedis;
    }

    public static JedisPooled getJedisSubscribe() {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return instance.jedisSubscribe;
    }

    public static String formatJedisKey(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return plugin.getName() + ":" + key;
    }

    public static void jedisSet(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        getJedis().set(formatJedisKey(plugin, key), value);
    }

    public static void jedisSetObject(Plugin plugin, String key, Object value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        getJedis().set(formatJedisKey(plugin, key), GsonUtils.parseObject(value));
    }

    public static <T> Object jedisGetObject(Plugin plugin, String key, Class<T> clazz) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) ? GsonUtils.parseJSON(getJedis().get(formatJedisKey(plugin, key)), clazz) : null;
    }

    public static void jedisSetSet(Plugin plugin, String key, Set<String> set) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        key = formatJedisKey(plugin, key);
        getJedis().del(key);
        getJedis().sadd(key, set.toArray(String[]::new));
    }

    public static void jedisSetList(Plugin plugin, String key, List<String> list) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        key = formatJedisKey(plugin, key);
        getJedis().del(key);
        getJedis().rpush(key, list.toArray(String[]::new));
    }

    public static void jedisAddInList(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            jedisSetList(plugin, key, List.of(value));
            return;
        }
        getJedis().lpush(formatJedisKey(plugin, key), value);
    }

    public static void jedisAddInSet(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            jedisSetSet(plugin, key, Set.of(value));
            return;
        }
        getJedis().sadd(formatJedisKey(plugin, key), value);
    }

    public static void jedisRemoveFromList(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            return;
        }
        getJedis().lrem(formatJedisKey(plugin, key), 1, value);
    }

    public static void jedisRemoveFromSet(Plugin plugin, String key, String... values) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            return;
        }
        getJedis().srem(formatJedisKey(plugin, key), values);
    }

    public static List<String> jedisGetList(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) ? getJedis().lrange(formatJedisKey(plugin, key), 0, getJedis().llen(formatJedisKey(plugin, key))) : Collections.emptyList();
    }

    public static Set<String> jedisGetSet(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) ? getJedis().sunion(formatJedisKey(plugin, key)) : new HashSet<>();
    }

    public static boolean jedisContainsInList(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) && getJedis().lpos(formatJedisKey(plugin, key), value) != null;
    }

    public static boolean jedisContainsInSet(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) && getJedis().sismember(formatJedisKey(plugin, key), value);
    }

    public static boolean jedisKeyExists(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return getJedis().exists(formatJedisKey(plugin, key));
    }

    public static String jedisGet(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return getJedis().get(formatJedisKey(plugin, key));
    }

    public static void jedisDel(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        getJedis().del(formatJedisKey(plugin, key));
    }

    public static void subscribe(Plugin plugin, String channelName, JedisPubSub jedisPubSub) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        registeredPubSubs.computeIfAbsent(plugin.getName(), k -> new HashMap<>()).put(formatJedisKey(plugin, channelName), jedisPubSub);
        ScheduleUtils.runTaskAsynchronously(() -> JedisUtils.getJedisSubscribe().subscribe(jedisPubSub, formatJedisKey(plugin, channelName)));
    }

    /**
     * Format: serverUuid;message
     */
    public static void jedisSend(Plugin plugin, String channelName, String message) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        getJedisSubscribe().publish(formatJedisKey(plugin, channelName), BeefLibrary.getServerUuid() + ";" + message);
    }

    public static void unSubscribe(Plugin plugin) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }

        registeredPubSubs.getOrDefault(plugin.getName(), new HashMap<>()).forEach((key, value) -> value.unsubscribe(key));
    }

    public static void unSubscribeAll() {
        registeredPubSubs.forEach((s, jedisPubSubs) -> jedisPubSubs.forEach((key, value) -> value.unsubscribe(key)));

    }

}
