package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Advertising;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdverisingDao extends JpaRepository<Advertising,Long> {
    List<Advertising> findAdvertisingByType(int type);
}
