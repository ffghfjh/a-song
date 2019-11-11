package com.fangzhou.asong.controller;

import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import com.fangzhou.asong.util.UserLoginToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/producter")
public class ProducterController {
    Logger logger = LoggerFactory.getLogger(ProducterController.class);

    @Autowired
    ProducterService producterService;

    @PostMapping("/good")
    @UserLoginToken
    public Result productGood(Long proId,HttpServletRequest request){
        logger.info("proId:"+proId);
        String token = request.getHeader("token");
        if(proId==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        return producterService.proGood(proId,token);
    }

    @PostMapping("/commont")
    @UserLoginToken
    public Result commont(Long proId,String context,HttpServletRequest request){
        String token  =request.getHeader("token");
        if(proId==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
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
    @UserLoginToken
    public Result replyGood(Long replyId,HttpServletRequest request){
        return producterService.replyGood(replyId,request.getHeader("token"));
    }
    @GetMapping("/getCommons")
    @UserLoginToken
    public Result getCommons(Long proId,HttpServletRequest request){
        return producterService.getProductCommont(proId,request.getHeader("token"));
    }

    @GetMapping("/getHotProducts")
    @UserLoginToken
    public Result getHotProducts(int count,HttpServletRequest request){
        logger.info("getLatestProduct");
        return producterService.getHotProduct(count,request.getHeader("token"));
    }


    @GetMapping("/getHotProductsByType")
    @UserLoginToken
    public Result getHotProductsByType(int count,HttpServletRequest request){
        return producterService.getHotProductsByType(count,request.getHeader("token"));
    }

    @GetMapping("/getLatestProduct")
    @UserLoginToken
    public Result getLatestProduct(int count,HttpServletRequest request){
        logger.info("getLatestProduct");
        return producterService.getLatestProduct(count,request.getHeader("token"));
    }


    @GetMapping("/getLatestProductByType")
    @UserLoginToken
    public Result getLatestProductByType(int type,int count,HttpServletRequest request){
        logger.info("getLatestProductByType");
        return producterService.getLatestProductByType(type,count,request.getHeader("token"));
    }


    @GetMapping("/getProducts")
    @UserLoginToken
    public Result getProducts(Integer count,HttpServletRequest request){
        logger.info("getProducts");
        if(count==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        return producterService.getProducts(count,request.getHeader("token"));
    }



    @GetMapping("/getProductsByType")
    @UserLoginToken
    public Result getProductsByType(Integer type,HttpServletRequest request){
        logger.info("getProductsByType");
        return producterService.getProjuctsByType(type,request.getHeader("token"));
    }


    @GetMapping("/getHotAuthors")
    @UserLoginToken
    public Result getHotAuthors(Integer count){
        logger.info("getHotAuthors");
        if(count==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        return producterService.getHotAuthors(count);
    }

    @PostMapping("/addPlay")
    @UserLoginToken
    public Result addPlay(Long proId,HttpServletRequest request){
        return producterService.addPlay(proId,request.getHeader("token"));
    }

    @PostMapping("/addShare")
    @UserLoginToken
    public Result addShare(Long proId,HttpServletRequest request){
        return producterService.addShare(proId,request.getHeader("token"));
    }

}
