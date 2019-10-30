package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.service.RedisService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisServiceImpl implements RedisService {
    Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean addProduct(Long productId) {
        redisTemplate.opsForSet().add("product",productId);
        logger.info("redis加入作品信息成功,id:"+productId);
        return true;
    }

    @Override
    public Set<Long> getProduct() {

        Set<Integer> set =   redisTemplate.opsForSet().members("product");
        for(Integer l : set){
            System.out.println(l);
        }
        return null;
    }


    @Override
    public String getOpenId(String str) {
        return (str.split("-"))[0];
    }

    @Override
    public String getSessionKey(String str) {
        return (str.split("-"))[1];
    }

    @Override
    public String getUserId(String str) {
        return (str.split("-"))[2];
    }

}
