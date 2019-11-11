package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;

public interface UserService {
    /**
     * 微信登录
     * @param code
     * @return
     */
    @Transactional
    Result login(String code,String signature,String encryptedData,String iv);

    /**
     * 获取我的信息
     * @param token
     * @return
     */
    Result getMyInfo(String token);


    @Transactional
    Result AuthorRequest(String name, String idCard, String referrals, String province,
                         String city, String district, MultipartFile cardImg, String token);

    /**
     * 第一次发布作品
     * @param name
     * @param type
     * @param file
     * @param token
     * @param city
     * @param reference
     * @param age
     * @return
     */
    @Transactional
    Result releaseProductNoAu(String name,int type,String time,MultipartFile file,String token,String prov,String city,String reference,int age);

    /**
     * 作者发布作品
     * @param name
     * @param type
     * @param file
     * @param token
     * @return
     */
    Result releaseProduct(String name,int type,String time,MultipartFile file,String token);
    /**
     * 关注用户
     * @param token
     * @param userId
     * @return
     */
    @Transactional
    Result noticeUser(String token,Long userId);

    /**
     * 取消关注
     * @param token
     * @param userId
     * @return
     */
    @Transactional
    Result notNoticeUser(String token,Long userId);

    /**
     * 我的关注
     * @param token
     * @return
     */
    @Transactional
    Result getMyNotice(String token);

    /**
     * 收听数据
     * @param token
     * @return
     */
    @Transactional
    Result getMyData(String token);

    /**
     * 日期间我的收听数据
     * @param start
     * @param end
     * @param token
     * @return
     */
    Result getMyDataByDate(String start, String end,String token);

    /**
     * 查询是否是作者
     * @param token
     * @return
     */
    Result getIsAuthor(String token);

    /**
     * 查询消息
     * @param token
     * @return
     */
    Result getMessages(String token);

    /**
     * 小程序支付回调
     * @param request
     * @return
     */
    void payResult(HttpServletRequest request, HttpServletResponse response);

    /**
     * 我关注的作者的作品
     * @param token
     * @return
     */
    Result getMyNoticeProduct(String token);

    /**
     * 获取我购买的作品
     * @param token
     * @return
     */
    Result getMyBuyProduct(String token);

    /**
     * 我的播放记录
     * @param token

     * @return
     */
    Result getMyPlayProduct(String token);

    /**
     * 获取我的订单信息
     * @param token
     * @return
     */
    Result getMyOrderInfo(String token);


}
