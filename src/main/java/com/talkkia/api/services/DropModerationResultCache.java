package com.talkkia.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DropModerationResultCache {

    private static final Duration RESULT_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "talkkia:drop:moderation:result:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void record(Long dropId, boolean approved) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        stringRedisTemplate.opsForValue().set(
                KEY_PREFIX + dropId,
                approved ? "1" : "0",
                RESULT_TTL
        );
    }

    public String get(Long dropId) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        return stringRedisTemplate.opsForValue().get(KEY_PREFIX + dropId);
    }
}
