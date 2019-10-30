package com.fangzhou.asong.controller;

import com.fangzhou.asong.dao.TestDao;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.JwtTokenUtil;
import com.fangzhou.asong.util.PassToken;
import com.fangzhou.asong.util.UserLoginToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {
    @Resource
    TestDao testDao;

    @Autowired
    RedisService redisService;


    @GetMapping("/test")
    public String test() {
        String token = JwtTokenUtil.createJWT(1000000000);
        return token;
    }

    @GetMapping("/test1")
    @UserLoginToken
    public String test1(String toekn) {

        return  "测试token验证：success";

    }
    @GetMapping("/priAllPro")
    public String priAllPro() {
        redisService.getProduct();
        return  "success";

    }


}
