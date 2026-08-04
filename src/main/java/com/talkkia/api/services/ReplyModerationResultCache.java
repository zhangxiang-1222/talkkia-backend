package com.talkkia.api.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReplyModerationResultCache {

    private static final Duration RESULT_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "talkkia:reply:moderation:result:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void record(Long replyId, boolean approved) {
        validateReplyId(replyId);
        stringRedisTemplate.opsForValue().set(
                KEY_PREFIX + replyId,
                approved ? "1" : "0",
                RESULT_TTL
        );
    }

    public String get(Long replyId) {
        validateReplyId(replyId);
        return stringRedisTemplate.opsForValue().get(KEY_PREFIX + replyId);
    }

    private void validateReplyId(Long replyId) {
        if (replyId == null || replyId <= 0) {
            throw new IllegalArgumentException("Reply ID is required");
        }
    }
}
