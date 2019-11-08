package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ReplyGood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyGoodDao extends JpaRepository<ReplyGood,Long> {

    ReplyGood findReplyGoodById(Long id);

    ReplyGood findReplyGoodByRepIdAndUserId(Long repId, Long userId);
}
