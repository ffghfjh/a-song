package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;

public interface ProducterService {

    /**
     * 点赞
     * @param proId
     * @param token
     * @return
     */
    Result proGood(Long proId,String token);
}
