package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProGood;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
@CacheConfig(cacheNames = "productGood")
public interface ProGoodDao extends CrudRepository<ProGood,Long> {


    List<ProGood> findProGoodsByUserId(Long userId);

    ProGood findProGoodByProIdAndUserId(Long proId,Long userId);

    List<ProGood> findProGoodsByUserIdAndState(Long userId,int state);

    List<ProGood> findProGoodsByUserIdAndStateAndCreateTime(Long userId, int state, Date time);
    @Override
    <S extends ProGood> S save(S s);
}
