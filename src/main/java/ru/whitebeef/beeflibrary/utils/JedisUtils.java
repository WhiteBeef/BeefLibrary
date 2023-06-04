package ru.whitebeef.beeflibrary.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.JedisPooled;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JedisUtils {

    private JedisPooled jedis = null;

    private static JedisUtils instance;

    public static JedisUtils getInstance() {
        return instance;
    }

    public JedisUtils() {
        instance = this;
        loadRedis();
    }

    private void loadRedis() {
        FileConfiguration config = BeefLibrary.getInstance().getConfig();
        if (config.getBoolean("redis.enable")) {
            jedis = new JedisPooled(config.getString("redis.host"), config.getInt("redis.port"), config.getString("redis.user"), config.getString("redis.password"));
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

    public static void jedisSetCollection(Plugin plugin, String key, Set<String> set) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        getJedis().rpush(formatJedisKey(plugin, key), set.toArray(String[]::new));
    }

    public static void jedisAddInCollection(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            jedisSetCollection(plugin, key, Set.of(value));
            return;
        }
        getJedis().lpush(formatJedisKey(plugin, key), value);
    }

    public static void jedisRemoveFromCollection(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        if (!jedisKeyExists(plugin, key)) {
            return;
        }
        getJedis().lrem(formatJedisKey(plugin, key), 1, value);
    }

    public static List<String> jedisGetCollection(Plugin plugin, String key) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) ? getJedis().lrange(formatJedisKey(plugin, key), 0, getJedis().llen(formatJedisKey(plugin, key))) : Collections.emptyList();
    }

    public static boolean jedisContainsCollection(Plugin plugin, String key, String value) {
        if (!isJedisEnabled()) {
            throw new RuntimeException("Jedis is not enabled!");
        }
        return jedisKeyExists(plugin, key) && getJedis().lpos(formatJedisKey(plugin, key), value) != null;
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

}
