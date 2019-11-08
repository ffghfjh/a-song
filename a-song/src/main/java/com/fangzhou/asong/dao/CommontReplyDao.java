package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProComReply;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommontReplyDao extends CrudRepository<ProComReply,Long> {
    List<ProComReply> findProComRepliesByReplyForIdAndReplyForType(Long forId,int type);

    ProComReply findProComReplyById(Long reId);
}
