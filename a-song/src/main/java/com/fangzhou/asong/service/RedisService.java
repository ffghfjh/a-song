package com.fangzhou.asong.service;

import java.util.Set;

/**
 * redis有关服务
 */
public interface RedisService {
     /**
      * 添加作品idredis
      * @param product_id
      * @return
      */
     boolean addProduct(Long product_id);



     Set<Long> getProduct();

     String getOpenId(String str);

     String getSessionKey(String str);

     String getUserId(String str);

}
