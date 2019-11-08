package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingDao extends JpaRepository<Setting,Long> {
    Setting findSettingByName(String name);
}
