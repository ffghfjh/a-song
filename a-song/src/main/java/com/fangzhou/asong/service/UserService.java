package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
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

    @Transactional
    Result releaseProduct(String name,int type,MultipartFile file,String token,String province,String city,String reference,int age);

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


}
