package org.redis;

import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisMap implements Map<String, String> {
    private final Jedis jedis;
    private static final String REDIS_KEY = "redis_map";

    public RedisMap(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    @Override
    public int size() {
        return (int) jedis.hlen(REDIS_KEY);
    }

    @Override
    public boolean isEmpty() {
        return jedis.hlen(REDIS_KEY) == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            return false;
        }
        return jedis.hexists(REDIS_KEY, key.toString());
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        return jedis.hvals(REDIS_KEY).contains(value.toString());
    }

    @Override
    public String get(Object key) {
        if (key == null) {
            return null;
        }
        return jedis.hget(REDIS_KEY, key.toString());
    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException("Ключ или значение null, не поддерживается Redis.");
        }
        String oldVal = jedis.hget(REDIS_KEY, key);
        jedis.hset(REDIS_KEY, key, value);
        return oldVal;
    }

    @Override
    public String remove(Object key) {
        if (key == null) {
            return null;
        }
        String oldVal = jedis.hget(REDIS_KEY, key.toString());
        jedis.hdel(REDIS_KEY, key.toString());
        return oldVal;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        if (m == null) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new NullPointerException("Ключ или значение null, не поддерживается Redis.");
            }
            map.put(entry.getKey(), entry.getValue());
        }
        jedis.hmset(REDIS_KEY, map);
    }

    @Override
    public void clear() {
        jedis.del(REDIS_KEY);
    }

    @Override
    public Set<String> keySet() {
        return jedis.hkeys(REDIS_KEY);
    }

    @Override
    public Collection<String> values() {
        return jedis.hvals(REDIS_KEY);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return jedis.hgetAll(REDIS_KEY).entrySet();
    }
}
