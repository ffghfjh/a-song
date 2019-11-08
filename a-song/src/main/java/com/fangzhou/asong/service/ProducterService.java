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
     * 获取最热
     * @return
     */
    Result getHotProduct(int count);

    /**
     * 根据分类获取最热作品
     * @return
     */
    Result getHotProductsByType(int count);

    /**
     * 获取我喜欢的作品
     * @param token
     * @return
     */
    Result getMyGoodProduct(String token);

    /**
     * 获取最新作品
     * @return
     */
    Result getLatestProduct(int count);

    /**
     * 获取推荐作者
     * @return
     */
    Result getAuthors();

    /**
     * 获取推荐作品
     * @return
     */
    Result getProducts(int count);

    /**
     * 根据分类获取最新作品
     * @param type
     * @return
     */
    Result getLatestProductByType(int type,int count);

    /**
     * 获取热门作者
     * @return
     */
    Result getHotAuthors();


    /**
     * 获取分类推荐作品
     * @param type
     * @return
     */
    Result getProjuctsByType(int type);
    /**
     * 根据ID获取作品信息i
     * @param proId
     * @return
     */
    Result getProductById(Long proId,String token);

    /**
     * 播放
     * @param proId
     * @param token
     * @return
     */
    Result addPlay(Long proId,String token);

    /**
     * 分享
     * @param proId
     * @param token
     * @return
     */
    Result addShare(Long proId,String token);
}
