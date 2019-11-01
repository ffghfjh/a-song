package com.fangzhou.asong.service;

import com.fangzhou.asong.util.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

public interface UserService {
    /**
     * 微信登录
     * @param code
     * @return
     */
    @Transactional
    Result login(String code,String signature,String encryptedData,String iv);

    @Transactional
    Result AuthorRequest(String name, String idCard, String referrals, String province,
                         String city, String district, MultipartFile cardImg, String token);

    @Transactional
    Result releaseProduct(String name,int type,MultipartFile file,String token);
}
