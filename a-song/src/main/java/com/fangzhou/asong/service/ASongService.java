package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ASongService {
    Result getProClass();

    /**
     * 添加意见反馈
     * @param context
     * @param name
     * @param token
     * @return
     */
    Result addFaceBack(String context,String name,String token);


    /**
     * 获取我的收听数据
     * @param token
     * @return
     */
    Result getMyASongData(String token);

    /**
     * 添加播放记录
     * @param proId
     * @param token
     * @return
     */
    Result addPlay(Long proId,String token);

    /**
     *
     * 下载作品
     * @param proId
     * @param token
     */
    Result downLoadProduct(Long proId, String token, HttpServletResponse response);

    /**
     * 获取贡献最大的作者
     * @return
     */
    Result getHotAuthors();

    /**
     * 获取广告
     * @return
     */
    Result getAdvertising();

    /**
     * 下单
     * @return
     */
    Result getOrder(String token, List<Long> proIds);

    /**
     * 获取用户的歌单列表
     * @return
     */
    Result getUsersProduct(Long authorId);
}
