package com.fangzhou.asong.controller;

import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.UserService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import com.fangzhou.asong.util.UserLoginToken;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

@RestController
@Api
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;
    @Autowired
    FileService fileService;




    @GetMapping("/login")
    public Result login(String code){
       return userService.login(code);
    }

    @PostMapping("/reqAuthor")
    @UserLoginToken
    public Result requestAuhor(String name, String idCard, String referrals, String province,
                               String city, String district, @RequestParam("cardImg") MultipartFile cardImg,
                               HttpServletRequest request){

        //图片
        if(fileService.getFileType(cardImg)==1){
            return userService.AuthorRequest(name,idCard,referrals,province,city,district,cardImg,request.getHeader("token"));
        }
         Result result= Result.failure(ResultCode.FAILURE);
         result.setMsg("文件格式不正确");
         return result;
    }



    @PostMapping("/releaseProduct")
    @UserLoginToken
    public Result releaseProduct(String name,int typeId,@RequestParam("file") MultipartFile file,HttpServletRequest request){
        logger.info("文件格式："+file.getContentType());
        String token = request.getHeader("token");
        //音频判断
        if(fileService.getFileType(file)==2){
            return userService.releaseProduct(name,typeId,file,token);
        }
        Result result= Result.failure(ResultCode.FAILURE);
        result.setMsg("文件格式不正确");
        return result;
    }





}
