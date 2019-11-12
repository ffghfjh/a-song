package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Play;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PlayDao extends JpaRepository<Play,Long> {

    //查询时间范围内的
    String sql = "select * from play where user_id = ?1 and year(create_time) = ?2 and month(create_time) = ?3 and day(create_time) = ?4";
    List<Play> findPlaysByUserId(Long userId);
    @Query(value = sql,nativeQuery = true)
    List<Play> findPlaysByDate(Long userId, String year, String month,String day);
}
