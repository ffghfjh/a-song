package com.fangzhou.asong.controller;

import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.service.UserService;
import com.fangzhou.asong.util.PassToken;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import com.fangzhou.asong.util.UserLoginToken;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@Api
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;
    @Autowired
    FileService fileService;
    @Autowired
    ASongService aSongService;
    @Autowired
    ProducterService producterService;




    @GetMapping("/login")
    @PassToken
    public Result login(String code,String signature,String encryptedData,String iv){
        logger.info("code:"+code+",sihnature:"+signature+",encryptedData:"+encryptedData+",iv:"+iv);
        return userService.login(code,signature,encryptedData,iv);
    }

    @GetMapping("/getMyInfo")
    @PassToken
    public Result login(HttpServletRequest request){
        return userService.getMyInfo(request.getHeader("token"));
    }

    @PostMapping("/notNotice")
    @UserLoginToken
    public Result notNotice(Long userId,HttpServletRequest request){
        return userService.notNoticeUser(request.getHeader("token"),userId);
    }
    @PostMapping("/notice")
    @UserLoginToken
    public Result notice(Long userId,HttpServletRequest request){
        logger.info("userId:"+userId);
        if(userId==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        return userService.noticeUser(request.getHeader("token"),userId);
    }








    @GetMapping("/getUserProducts")
    @UserLoginToken
    public Result getMyProducts(Long authorId){
        return aSongService.getUsersProduct(authorId);
    }

    @GetMapping("/getProductById")
    @UserLoginToken
    public Result getProductById(Long proId,HttpServletRequest request){
        return producterService.getProductById(proId,request.getHeader("token"));
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

    @PostMapping("/getOrder")
    @UserLoginToken
    public Result getOrder(List<Long> proIds, HttpServletRequest request){
        String token = request.getHeader("token");
        return aSongService.getOrder(token,proIds);
    }


    @PostMapping("/releaseProduct")
    @UserLoginToken
    public Result releaseProduct(String name,int typeId,@RequestParam("file") MultipartFile file,String province,String city,String reference,int age,HttpServletRequest request){
        logger.info("文件格式："+file.getContentType());
        String token = request.getHeader("token");
        //音频判断
        if(fileService.getFileType(file)==2){
            return userService.releaseProduct(name,typeId,file,token,province,city,reference,age);
        }
        Result result= Result.failure(ResultCode.FAILURE);
        result.setMsg("文件格式不正确");
        return result;
    }

    @GetMapping("/getMyData")
    @UserLoginToken
    public Result getMyData(HttpServletRequest request){
        return userService.getMyData(request.getHeader("token"));
    }



}
