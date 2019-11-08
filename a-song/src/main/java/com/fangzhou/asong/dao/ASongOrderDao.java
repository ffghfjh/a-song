package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ASongOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ASongOrderDao extends CrudRepository<ASongOrder,Long> {
    ASongOrder findASongOrderByProductIdLikeAndUserIdAndState(String proId,Long userId,int state);

    List<ASongOrder> findASongOrdersByProductIdLikeAndState(String proId, int state);

}
