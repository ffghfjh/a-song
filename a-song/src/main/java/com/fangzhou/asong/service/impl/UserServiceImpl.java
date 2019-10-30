package com.fangzhou.asong.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.fangzhou.asong.dao.AuthorDao;
import com.fangzhou.asong.dao.ProductDao;
import com.fangzhou.asong.dao.UserDao;
import com.fangzhou.asong.pojo.Author;
import com.fangzhou.asong.pojo.Product;
import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.service.UserService;
import com.fangzhou.asong.util.HttpUtil;
import com.fangzhou.asong.util.JwtTokenUtil;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisService redisService;

    @Autowired
    UserDao userDao;
    @Autowired
    AuthorDao authorDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    FileService fileService;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${wxchat.appid}")
    String appid;
    @Value("${wxchat.secret}")
    String secret;

    @Override
    public Result login(String code) {

        Result result = new Result();

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + secret + "&js_code=JSCODE&grant_type=authorization_code";
        JSONObject jsonObject = HttpUtil.getToJSONObject(url);

        logger.info(jsonObject.toString());

        int errcode = jsonObject.getInteger("errcode");

        String context = null;
        switch (errcode) {
            case 0:
                String openid = jsonObject.getString("openid");
                String session_key = jsonObject.getString("session_key");
                result.setCode(1);
                result.setMsg("成功");
                String token = JwtTokenUtil.createJWT(604800000);

                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                User user = userDao.findUserByOpenid(openid);

                /**
                 * 判断是否存在该用户
                 */
                if (user != null) {
                    map.put("userId", user.getId());
                    //关联token和微信openid和session_key
                    redisTemplate.opsForValue().set(token, openid + "-" + session_key+"-"+user.getId());
                } else {
                    User newUser = new User();
                    Date date = new Date();
                    newUser.setCreateTime(date);
                    newUser.setUpdateTime(date);
                    newUser.setOpenid(openid);
                    User user1 = userDao.save(newUser);
                    map.put("userId", user.getId());
                    redisTemplate.opsForValue().set(token, openid + "-" + session_key+"-"+user.getId());
                }
                result.setData(map);
                break;
            case -1:
                context = "微信系统繁忙";
                result.setCode(0);
                result.setMsg(context);
                break;
            case 40029:
                context = "code无效";
                result.setCode(0);
                result.setMsg(context);
                break;
            case 45011:
                context = "登录频率限制";
                result.setCode(0);
                result.setMsg(context);
                break;
            case 40013:
                context = "appid错误";
                result.setCode(0);
                result.setMsg(context);
                logger.error(context);
                break;
        }
        return result;
    }

    @Override
    public Result AuthorRequest(String name, String idCard, String referrals, String province,
                                String city, String district, MultipartFile cardImg, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        Result result;
        if (str != null) {
            String openid = redisService.getOpenId(str);
            logger.info("openid:" + openid);
            String session_key = redisService.getSessionKey(str);
            User user = userDao.findUserByOpenid(openid);
            if (user != null) {
                if (!user.isAuthor()) {
                    Author author1 = authorDao.findAuthorByUserIdAndState(user.getId(), 0);
                    //检查是否提交过审核
                    if (author1 == null) {
                        String url = fileService.saveFile(cardImg, cardImg.getContentType().split("/")[1]);
                        logger.info("文件上传：" + url);
                        if (url != null) {
                            Author author = new Author();
                            Date date = new Date();
                            author.setCardUrl(url);
                            author.setCdCard(idCard);
                            author.setCity(city);
                            author.setUserId(user.getId());
                            author.setDistrict(district);
                            author.setName(name);
                            author.setProvince(province);
                            author.setState(0);
                            author.setCreateTime(date);
                            author.setUpdateTime(date);
                            authorDao.save(author);
                            return Result.success();
                        }
                    } else {
                        result = Result.failure(ResultCode.FAILURE);
                        result.setMsg("资料待审核中");
                        return result;
                    }
                }
                result = Result.failure(ResultCode.FAILURE);
                result.setMsg("已是作者");
                return result;
            }
            logger.info("用户为P空");
        }
        result = Result.failure(ResultCode.FAILURE);
        return result;
    }

    @Override
    public Result releaseProduct(String name, int type, MultipartFile file,String token) {
        Result result;
        String str = stringRedisTemplate.opsForValue().get(token);
        logger.info("openId:"+redisService.getOpenId(str));
        if(str!=null){
            User user = userDao.findUserByOpenid(redisService.getOpenId(str));
            if(user!=null){
                if(user.getState()==1){
                    if(user.isAuthor()){
                        String url = fileService.saveFile(file,file.getContentType().split("/")[1]);//作品地址
                        Author author = authorDao.findAuthorByUserIdAndState(user.getId(),Author.PASS);
                        Product product = new Product();
                        Date date = new Date();
                        product.setAuthorId(author.getId());
                        product.setClassId(type);
                        product.setState(Product.ONSHELF);
                        product.setProUrl(url);
                        product.setCreateTime(date);
                        product.setUpdateTime(date);
                        Product product1 = productDao.save(product);
                        redisService.addProduct(product1.getId());
                        return Result.success();
                    }
                    result = Result.failure(ResultCode.FAILURE);
                    result.setMsg("非作者");
                    return result;
                }
                result = Result.failure(ResultCode.FAILURE);
                result.setMsg("被封号");
                return result;
            }
        }
        result = Result.failure(ResultCode.FAILURE);
        return result;
    }


    @Test
    public void getProducts(){

    }



}
