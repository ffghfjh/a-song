package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.CommontGood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommontGoodDao extends JpaRepository<CommontGood,Long> {

    CommontGood findCommontGoodByComIdAndUserId(Long comId,Long userId);

    List<CommontGood> findCommontGoodsByComId(Long commId);
}
