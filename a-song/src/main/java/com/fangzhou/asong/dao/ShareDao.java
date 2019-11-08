package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShareDao extends JpaRepository<Share,Long> {

    List<Share> findSharesByUserId(Long userId);
}
