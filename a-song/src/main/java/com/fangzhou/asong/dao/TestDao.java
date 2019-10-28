package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestDao  extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
}
