package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User,Long> {
    User findUserByOpenid(String openId);
}
