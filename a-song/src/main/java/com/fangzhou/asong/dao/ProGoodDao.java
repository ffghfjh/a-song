package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProGood;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
@CacheConfig(cacheNames = "productGood")
public interface ProGoodDao extends CrudRepository<ProGood,Long> {

    String sql = "select * from pro_good where user_id = ?1 and state = ?2 and year(update_time) = ?3 and month(update_time) = ?4 and day(update_time) = ?5";

    List<ProGood> findProGoodsByUserId(Long userId);

    ProGood findProGoodByProIdAndUserId(Long proId,Long userId);

    List<ProGood> findProGoodsByUserIdAndState(Long userId,int state);

    @Query(value = sql,nativeQuery = true)
    List<ProGood> findGoodsByDate(Long userId, int state, String year,String month,String day);
    @Override
    <S extends ProGood> S save(S s);
}
