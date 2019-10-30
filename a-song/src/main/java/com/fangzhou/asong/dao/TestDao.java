package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface TestDao  extends JpaRepository<User,Long>{




}
