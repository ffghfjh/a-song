package com.fangzhou.asong.controller;

import com.fangzhou.asong.dao.AuthorDao;
import com.fangzhou.asong.pojo.Author;
import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.service.UserService;
import com.fangzhou.asong.util.*;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import net.bytebuddy.asm.Advice;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    @Autowired
    AuthorDao authorDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;



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
    public Result getMyProducts(Long authorId,HttpServletRequest request){
        return aSongService.getUsersProduct(authorId,request.getHeader("token"));
    }

    @GetMapping("/getMyProducts")
    @UserLoginToken
    public Result getMyProducts(HttpServletRequest request){
        String token = request.getHeader("token");
        if(token==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        String str = stringRedisTemplate.opsForValue().get(token);
        Long userId = Long.parseLong(str.split("%")[2]);
        logger.info("userId:"+userId);
        Author author = authorDao.findAuthorByUserIdAndState(userId,1);
        if(author==null){
            logger.info("author is null");
            return Result.success();
        }
        return aSongService.getUsersProduct(author.getId(),token);
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
    public Result getOrder(@RequestParam(value = "proIds")List<Long> proIds, HttpServletRequest request){
         if(proIds.isEmpty()){
             return Result.failure(ResultCode.PARAM_IS_BLANK);
         }
        String token = request.getHeader("token");
        return aSongService.getOrder(token,proIds);
    }

    @PostMapping("/payCallback")
    @UserLoginToken
    public void payCallback(HttpServletRequest request, HttpServletResponse response){
        logger.info("微信支付回调");
         userService.payResult(request,response);
    }


    @PostMapping("/releaseProductNoAuthor")
    @PassToken
    public Result releaseProductNoAuthor(String name,Integer typeId,String time,@RequestParam("file") MultipartFile file,String token,String prov,String city,String reference,int age){
        logger.info("进入发布作品1");
        if(name==null||typeId==null||time==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        logger.info("进入发布作品2");
        try{
            Claims clains = JwtTokenUtil.parseJWT(token);
            if(clains.getIssuer().equals("dengshilin")){  //验证签发者
                logger.info("文件格式："+file.getContentType());

                //音频判断
                if(fileService.getFileType(file)==2){
                    return userService.releaseProductNoAu(name,typeId,time,file,token,prov,city,reference,age);
                }
                Result result= Result.failure(ResultCode.FAILURE);
                result.setMsg("文件格式不正确");
                return result;
            }else {
                logger.info("签发者验证失败");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
       return Result.failure(ResultCode.FAILURE);
    }

    @PostMapping("/releaseProduct")
    @PassToken
    public Result releaseProduc(String name,Integer typeId,String time,@RequestParam("file") MultipartFile file,String token){
        logger.info("发布作品，token1："+token);
        if(name==null||typeId==null||time==null||token==null){
            return Result.failure(ResultCode.PARAM_IS_BLANK);
        }
        logger.info("发布作品，token2："+token);
        try{
            Claims clains = JwtTokenUtil.parseJWT(token);
            if(clains.getIssuer().equals("dengshilin")){  //验证签发者
                logger.info("文件格式："+file.getContentType());
                //音频判断
                if(fileService.getFileType(file)==2){
                    return userService.releaseProduct(name,typeId,time,file,token);
                }
                Result result= Result.failure(ResultCode.FAILURE);
                result.setMsg("文件格式不正确");
                return result;
            }else {
                logger.info("签发者验证失败");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @GetMapping("/getMyData")
    @UserLoginToken
    public Result getMyData(HttpServletRequest request){
        return userService.getMyData(request.getHeader("token"));
    }

    @GetMapping("/getMyDataByDate")
    @UserLoginToken
    public Result getMyDataByDate(String start,String end,HttpServletRequest request){
        logger.info("start:"+start+","+"end:"+end);
        return userService.getMyDataByDate(start,end,request.getHeader("token"));
    }


    @GetMapping("/getMyLikeProduct")
    @UserLoginToken
    public Result getMyLikeProduct(HttpServletRequest request){
        return producterService.getMyGoodProduct(request.getHeader("token"));
    }

    @GetMapping("/getMyNoticeProduct")
    @UserLoginToken
    public Result getMyNoticeProduct(HttpServletRequest request){
        return userService.getMyNoticeProduct(request.getHeader("token"));
    }

    @GetMapping("/getIsAuthor")
    @UserLoginToken
    public Result getIsAuthor(HttpServletRequest request){
      return userService.getIsAuthor(request.getHeader("token"));
    }


    @GetMapping("/getMessage")
    @UserLoginToken
    public Result getMessage(HttpServletRequest request){
        return userService.getMessages(request.getHeader("token"));
    }


    @GetMapping("/getMyBuyProduct")
    @UserLoginToken
    public Result getMyBuyProduct(HttpServletRequest request){
        return userService.getMyBuyProduct(request.getHeader("token"));
    }

    @GetMapping("/getMyPlayProduct")
    @UserLoginToken
    public Result getMyPlayProduct(HttpServletRequest request){
        return userService.getMyPlayProduct(request.getHeader("token"));
    }


    @GetMapping("/getMyOrderInfo")
    @UserLoginToken
    public Result getMyOrderInfo(HttpServletRequest request){
        return userService.getMyOrderInfo(request.getHeader("token"));
    }

}
