package com.fiap.hackaton.grp14.consumer.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String getValue(String key) {
        Object value = redisTemplate.opsForHash().get("videos", key);
        return value != null ? value.toString() : null;
    }

    public void addToHash(String hashKey, String field, String value) {
        redisTemplate.opsForHash().put(hashKey, field, value);
    }

    public Object getFromHash(String hashKey, String field) {
        return redisTemplate.opsForHash().get(hashKey, field);
    }

    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    public Map<String, String> getAllVideoEntries() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries("videos");

            Map<String, String> result = new HashMap<>();
            entries.forEach((key, value) ->
                    result.put(key.toString(), value.toString()));

            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public List<String> getAllPaths() {
        return redisTemplate.opsForHash().values("videos")
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public Set<String> getSafeVideoKeys() {
        Set<Object> rawKeys = redisTemplate.opsForHash().keys("videos");
        if (rawKeys.isEmpty()) {
            return Collections.emptySet();
        }
        return rawKeys.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
