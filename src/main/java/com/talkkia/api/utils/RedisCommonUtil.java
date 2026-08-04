package com.talkkia.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @Author: Ethan
 * @Date: 2024/6/18
 * @Description: Generic utility class for Redis
 */
@Component
@Slf4j
public class RedisCommonUtil {

    @Resource
    private RedisTemplate redisTemplate;

// =============================common============================

    /**
     * Set the expiration time for a cache.
     *
     * @param key  The key
     * @param time The time (in seconds)
     * @return true if successful, false otherwise
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

    /**
     * Get the expiration time based on the key.
     *
     * @param key The key (cannot be null)
     * @return The time (in seconds). Returns 0 if the key is permanently valid.
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Check if a key exists.
     *
     * @param key The key
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

    /**
     * Delete cache.
     *
     * @param key One or more keys
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

// ============================String=============================

    /**
     * Get a cache value.
     *
     * @param key The key
     * @return The value
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * Set a cache value.
     *
     * @param key   The key
     * @param value The value
     * @return true if successful, false otherwise
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

    /**
     * Set a cache value and set the expiration time.
     *
     * @param key   The key
     * @param value The value
     * @param time  The time (in seconds). If time is less than or equal to 0, the cache will be set indefinitely.
     * @return true if successful, false otherwise
     */
    public boolean setEx(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

// ============================Increment=============================

    /**
     * 将 key 中储存的数字值增一。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 INCR 操作。
     *
     * @param key The key
     * @return 增加后的值
     */
    public long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 将 key 中储存的数字值增加指定数值。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0，然后再执行 INCRBY 操作。
     *
     * @param key   The key
     * @param value 要增加的值
     * @return 增加后的值
     */
    public long incrBy(String key, long value) {
        return redisTemplate.opsForValue().increment(key, value);
    }

    /**
     * 将 key 中储存的数字值增一，并设置过期时间（仅在 key 不存在时设置过期时间）
     *
     * @param key        The key
     * @param expireTime 过期时间（秒）
     * @return 增加后的值
     */
    public long incrWithExpire(String key, long expireTime) {
        Long result = redisTemplate.opsForValue().increment(key);
        // 只有在 key 是新建的情况下才设置过期时间
        if (result != null && result == 1) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
        return result;
    }

// ============================Set=============================

    /**
     * 向 Set 集合中添加元素
     *
     * @param key   The key
     * @param values 要添加的元素
     * @return 添加成功的元素数量
     */
    public long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 判断元素是否在 Set 集合中
     *
     * @param key   The key
     * @param value 要检查的元素
     * @return true 如果元素存在，false 如果不存在
     */
    public boolean sIsMember(String key, Object value) {
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return result != null && result;
    }

    /**
     * 获取 Set 集合中的所有元素
     *
     * @param key The key
     * @return Set 集合中的所有元素
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 获取 Set 集合的大小
     *
     * @param key The key
     * @return Set 集合的大小
     */
    public long sSize(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

    /**
     * 向 Set 集合中添加元素，并设置过期时间（仅在 key 不存在时设置过期时间）
     *
     * @param key        The key
     * @param value      要添加的元素
     * @param expireTime 过期时间（秒）
     * @return true 如果添加成功，false 如果元素已存在
     */
    public boolean sAddWithExpire(String key, Object value, long expireTime) {
        Boolean isNew = redisTemplate.opsForSet().add(key, value) > 0;
        // 只有在 key 是新建的情况下才设置过期时间
        if (isNew != null && isNew) {
            // 检查是否是第一次添加元素（集合大小为1）
            if (redisTemplate.opsForSet().size(key) == 1) {
                redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            }
            return true;
        }
        return false;
    }

}
