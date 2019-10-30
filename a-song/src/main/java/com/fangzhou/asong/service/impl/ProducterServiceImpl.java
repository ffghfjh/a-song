package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

public class ProducterServiceImpl implements ProducterService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisService redisService;
    @Override
    public Result proGood(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        int userId = Integer.parseInt(redisService.getUserId(str));
        
        return null;
    }
}
