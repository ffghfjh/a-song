package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;
import org.springframework.transaction.annotation.Transactional;

public interface ProducterService {

    /**
     * 作品点赞
     * @param proId
     * @param token
     * @return
     */
    @Transactional
    Result proGood(Long proId,String token);


    /**
     * 评论
     * @param proId
     * @param token
     * @return
     */
    @Transactional
    Result proCommont(Long proId,String context,String token);


    /**
     * 回复
     */
    @Transactional
    Result comReply(int type,Long forId,String context,String token);

    /**
     * 评论点赞
     * @return
     */
    @Transactional
    Result commontGood(Long commId,String token);

    /**
     * 回复点赞
     * @param replyId
     * @param token
     * @return
     */
    Result replyGood(Long replyId,String token);

    /**
     * 获取作品的所有评论和回复信息
     * @param proId
     * @return
     */
    Result getProductCommont(Long proId);

    /**
     * 获取推荐作品
     * @return
     */
    Result getProduct();
}
