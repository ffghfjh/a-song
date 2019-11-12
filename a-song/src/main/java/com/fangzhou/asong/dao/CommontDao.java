package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProCommont;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
@CacheConfig(cacheNames = "commons")
public interface CommontDao extends CrudRepository<ProCommont,Long> {
    String sql = "select * from pro_commont where user_id = :userId and year(create_time) = :year and month(create_time)= :month and day(create_time) = :day";
    List<ProCommont> findProCommontsByProId(Long proId);
    List<ProCommont> findProCommontsByUserId(Long userId);
    ProCommont findProCommontById(Long commId);
    @Query(value = sql,nativeQuery = true)
    List<ProCommont> getCommontsByData(@Param("userId") Long userId,@Param("year") String year, @Param("month") String month, @Param("day") String day);
}
