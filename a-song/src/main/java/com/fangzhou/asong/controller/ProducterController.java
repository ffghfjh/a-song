package com.fangzhou.asong.controller;

import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.UserLoginToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/producter")
public class ProducterController {
    @Autowired
    ProducterService producterService;

    @PutMapping("/good")
    @UserLoginToken
    public Result productGood(Long proId,HttpServletRequest request){
        String token = request.getHeader("token");
        return producterService.proGood(proId,token);
    }


    @PostMapping("/commont")
    @UserLoginToken
    public Result commont(Long proId,String context,HttpServletRequest request){
        String token  =request.getHeader("token");
        return producterService.proCommont(proId,context,token);
    }

    @PostMapping("/commontReply")
    @UserLoginToken
    public Result commontReply(int type,Long forId,String context,HttpServletRequest request){
        return producterService.comReply(type,forId,context,request.getHeader("token"));
    }

    @PostMapping("/commontGood")
    @UserLoginToken
    public Result commontGood(Long commId,HttpServletRequest request){
        return producterService.commontGood(commId,request.getHeader("token"));
    }

    @PostMapping("/replyGood")
    public Result replyGood(Long replyId,HttpServletRequest request){
        return producterService.replyGood(replyId,request.getHeader("token"));
    }

    @GetMapping("/getCommons")
    public Result getCommons(Long proId){
        return producterService.getProductCommont(proId);
    }

    @GetMapping("/getProducts")
    @UserLoginToken
    public Result getProducts(){
        return producterService.getProduct();
    }
}
