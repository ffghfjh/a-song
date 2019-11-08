package com.fangzhou.asong.controller;

import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.UserLoginToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/addFaceBack")
    @UserLoginToken
    public Result addFaceBack(String context, String name, HttpServletRequest request){
       return aSongService.addFaceBack(context,name,request.getHeader("token"));
    }

    @GetMapping("/download")
    @UserLoginToken
    public Result download(Long proId,HttpServletResponse response,HttpServletRequest request){
        return aSongService.downLoadProduct(proId,request.getHeader("token"),response);
    }

    @GetMapping("/getAuthors")
    public Result getAuthors(){
        logger.info("getAuthors");
        return producterService.getAuthors();
    }

    @GetMapping("/getAdvertising")
    public Result getAdvertising(){
        logger.info("getAdvertising");
        return aSongService.getAdvertising();
    }

}
