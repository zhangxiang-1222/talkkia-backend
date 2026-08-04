package com.talkkia.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author: Ethan
 * @Date: 2024/6/17
 * @Description: Redis config
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // Set serializer for String type keys
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Set serializer for Hash type keys
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Set the Lettuce connection factory
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        return redisTemplate;
    }

}
