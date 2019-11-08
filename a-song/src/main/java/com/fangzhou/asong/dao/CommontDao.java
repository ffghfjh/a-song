package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProCommont;
import com.fangzhou.asong.pojo.Product;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
@CacheConfig(cacheNames = "commons")
public interface CommontDao extends CrudRepository<ProCommont,Long> {
    List<ProCommont> findProCommontsByProId(Long proId);
    List<ProCommont> findProCommontsByUserId(Long userId);
    ProCommont findProCommontById(Long commId);
    List<ProCommont> findProCommontsByUserIdAndCreateTime(Long userId, Date time);
}
