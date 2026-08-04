package com.talkkia.api.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @Author: Ethan
 * @Date: 2024/8/12
 * @Description: This is a utility class for operating ZSet in Redis.
 */
@Component
public class RedisZSetUtil {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * Add an element. The biggest difference between zset and set is that each element has a score, so there is an auxiliary function for sorting; zadd
     *
     * @param key
     * @param value
     * @param score
     */
    public void add(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Delete element zrem
     *
     * @param key
     * @param value
     */
    public void remove(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * Increase or decrease of score zincrby
     *
     * @param key
     * @param value
     * @param score
     */
    public Double incrScore(String key, String value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    /**
     * Query the score zscore corresponding to value
     *
     * @param key
     * @param value
     * @return
     */
    public Double score(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * Determine the ranking of value in zset zrank
     *
     * @param key
     * @param value
     * @return
     */
    public Long rank(String key, String value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * Query the values ​​of the specified order in the collection, 0-1 means to get all the collection contents zrange
     *
     * Return an ordered collection, with the smaller score at the front
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> range(String key, int start, int end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Query the values ​​and scores of the specified order in the collection, 0, -1 means to get all the collection contents
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> rangeWithScore(String key, int start, int end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * Query the values ​​of the specified order in the collection zrevrange
     *
     * Returns an ordered set with the largest score at the front
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> revRange(String key, int start, int end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * Get the set that meets the conditions based on the score value zrangebyscore
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> sortRange(String key, int min, int max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * delete a zset
     * @param key
     */
    public void deleteZSet(String key) {
        redisTemplate.delete(key);
    }
}
