package com.fangzhou.asong.controller;

import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.UserLoginToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProducterController {

    @PutMapping
    @UserLoginToken
    public Result productGood(Long proId,HttpServletRequest request){

        String token = request.getHeader("token");
        return null;

    }



}
