package com.fangzhou.asong.controller;

import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.util.PassToken;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import com.fangzhou.asong.util.UserLoginToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/main")
public class ASongController {
    Logger logger = LoggerFactory.getLogger(ASongController.class);
    @Autowired
    ASongService aSongService;
    @Autowired
    ProducterService producterService;

    /**
     * 获取所有音乐分类
     * @return
     */
    @GetMapping("/getProClass")
    public Result getProClass(){
      return aSongService.getProClass();
    }

    @PostMapping("/addFaceBack")
    @UserLoginToken
    public Result addFaceBack(String context, String name, HttpServletRequest request){
       return aSongService.addFaceBack(context,name,request.getHeader("token"));
    }

    @GetMapping("/download")
    @PassToken
    public Result download(Long proId,String token,HttpServletResponse response){
        if(proId!=null){
           return aSongService.downLoadProduct(proId,token,response);
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @GetMapping("/getAuthors")
    public Result getAuthors(int count){
        logger.info("getAuthors");
        return producterService.getAuthors(count);
    }

    @GetMapping("/getAdvertising")
    public Result getAdvertising(){
        logger.info("getAdvertising");
        return aSongService.getAdvertising();
    }

    @GetMapping("/searchProduct")
    @UserLoginToken
    public Result searchProduct(String str,HttpServletRequest request){
        if(str==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        return producterService.searchProduct(str,request.getHeader("token"));
    }

}
