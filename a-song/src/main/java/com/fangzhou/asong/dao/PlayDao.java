package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Play;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PlayDao extends JpaRepository<Play,Long> {
    List<Play> findPlaysByUserId(Long userId);
    List<Play> findPlaysByUserIdAndCreateTime(Long userId, Date time);
}
