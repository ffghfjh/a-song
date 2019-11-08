package com.fangzhou.asong.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fangzhou.asong.dao.*;
import com.fangzhou.asong.pojo.*;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.service.UserService;
import com.fangzhou.asong.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RedisTemplate redisTemplate;
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

    @Autowired
    OwnProducterDao ownProducterDao;

    @Autowired
    CommontDao commontDao;

    @Autowired
    ProGoodDao proGoodDao;

    @Autowired
    PlayDao playDao;

    @Autowired
    ShareDao shareDao;

    @Autowired
    NoticeDao noticeDao;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${wxchat.appid}")
    String appid;
    @Value("${wxchat.secret}")
    String secret;


    @Override
    public Result login(String code, String signature, String encryptedData, String iv) {
        logger.info("code:" + code);
        Result result = new Result();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        JSONObject jsonObject = HttpUtil.getToJSONObject(url);
        logger.info(jsonObject.toString());
        try {
            String openId = jsonObject.getString("openid");
            String sessionKey = jsonObject.getString("session_key");
            result.setCode(1);
            result.setMsg("成功");
            String token = JwtTokenUtil.createJWT(604800000);

            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            User user = userDao.findUserByOpenid(openId);

            /**
             * 判断是否存在该用户
             */
            if (user != null) {
                map.put("userId", user.getId());
                map.put("header", user.getHeader());
                map.put("name", user.getName());
                //关联token和微信openid和session_key
                stringRedisTemplate.opsForValue().set(token, openId + "-" + sessionKey + "-" + user.getId());
            }
            //新用户
            else {
                logger.info(appid + "," + encryptedData + "," + sessionKey + "," + iv);
                String userInfo = WXCore.decrypt(appid, encryptedData, sessionKey, iv);
                logger.info(userInfo);
                try {
                    JSONObject info = JSON.parseObject(userInfo);
                    String nickName = info.getString("nickName");
                    int gender = info.getInteger("gender");
                    String city = info.getString("city");
                    String province = info.getString("province");
                    String header = info.getString("avatarUrl");

                    User newUser = new User();
                    Date date = new Date();
                    if (gender == 0 || gender == 1) {
                        newUser.setMan(true);
                    } else {
                        newUser.setMan(false);
                    }
                    newUser.setName(nickName);
                    newUser.setCity(city);
                    newUser.setProvince(province);
                    newUser.setHeader(header);
                    newUser.setCreateTime(date);
                    newUser.setUpdateTime(date);
                    newUser.setOpenid(openId);
                    User user1 = userDao.save(newUser);
                    map.put("userId", user1.getId());
                    map.put("header", user1.getHeader());
                    map.put("name", user1.getName());
                    stringRedisTemplate.opsForValue().set(token, openId + "-" + sessionKey + "-" + user1.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            result.setData(map);
        } catch (Exception e) {
            e.printStackTrace();
            int errcode = jsonObject.getInteger("errcode");
            String context = null;
            switch (errcode) {
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
        }
        return result;
    }

    @Override
    public Result getMyInfo(String token) {
        Map<String, Object> map = new HashMap<>();
        String str = stringRedisTemplate.opsForValue().get(token);
        logger.info(str);
        if (str != null) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            User user = userDao.findUserById(userId);
            map.put("header", user.getHeader());
            map.put("name", user.getName());
            if (user.isAuthor()) {
                Author author = authorDao.findAuthorByUserIdAndState(user.getId(), 1);
                AuthorOwnProduct ownProduct = ownProducterDao.findAuthorOwnProductByAuthorId(author.getId());
                int num = ownProduct.getNum();
                map.put("count", num);
            } else {
                map.put("count", 0);
            }
            return Result.success(map);
        }
        return Result.failure(ResultCode.FAILURE);
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
        }
        result = Result.failure(ResultCode.FAILURE);
        return result;
    }

    @Override
    public Result releaseProduct(String name, int type, MultipartFile file, String token,
                                 String province, String city, String reference, int age) {

        Result result;
        String str = stringRedisTemplate.opsForValue().get(token);
        logger.info("token:" + token + ",str:" + str);
        if (str != null && !str.equals("")) {
            logger.info("openId:" + redisService.getOpenId(str));
            User user = userDao.findUserByOpenid(redisService.getOpenId(str));
            if (user != null) {
                if (user.getState() == 1) {
                    if (!user.isAuthor()) {
                        Author author = new Author();
                        Date date = new Date();
                        author.setUserId(user.getId());
                        author.setProvince(user.getProvince());
                        author.setCity(user.getCity());
                        author.setDistrict(user.getDistrict());
                        author.setState(1);
                        author.setName(user.getName());
                        author.setCreateTime(date);
                        author.setUpdateTime(date);
                        authorDao.save(author);
                        user.setAuthor(true);
                        userDao.save(user);
                    }
                    String url = fileService.saveFile(file, file.getContentType().split("/")[1]);//作品地址
                    Author author = authorDao.findAuthorByUserIdAndState(user.getId(), Author.PASS);
                    Product product = new Product();
                    Date date = new Date();
                    product.setAuthorId(author.getId());
                    product.setClassId(type);
                    product.setState(Product.ONSHELF);
                    product.setProUrl(url);
                    product.setTitle(name);
                    product.setCreateTime(date);
                    product.setUpdateTime(date);
                    product.setProvince(province);
                    product.setCity(city);
                    product.setRefrence(reference);
                    product.setAge(age);
                    product.setGoodNum(0);
                    product.setPlayNum(0);
                    product.setDownNum(0);
                    product.setComNum(0);
                    product.setRecommend(false);//默认不推荐
                    product.setShareNum(0);
                    Product product1 = productDao.save(product);
                    //记录作者贡献的作品数
                    AuthorOwnProduct ownProduct = ownProducterDao.findAuthorOwnProductByAuthorId(author.getId());
                    if (ownProduct == null) {
                        AuthorOwnProduct ownProduct1 = new AuthorOwnProduct();
                        ownProduct1.setAuthorId(author.getId());
                        ownProduct1.setNum(1);
                        ownProduct1.setCreateTime(date);
                        ownProduct1.setUpdateTime(date);
                        ownProducterDao.save(ownProduct1);
                    } else {
                        ownProduct.setNum(ownProduct.getNum() + 1);
                        ownProducterDao.save(ownProduct);
                    }
                    redisService.addProduct(product1.getId());
                    return Result.success();
                }
                result = Result.failure(ResultCode.FAILURE);
                result.setMsg("被封号");
                return result;
            }
        }
        result = Result.failure(ResultCode.FAILURE);
        return result;
    }

    @Override
    public Result noticeUser(String token, Long userId) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (null != str) {
            Long id = Long.parseLong(redisService.getUserId(str));
            logger.info("id:"+id);
            User user = userDao.findUserById(userId);
            if (user == null) {
                return Result.failure(ResultCode.FAILURE);
            }
            redisTemplate.opsForSet().add("attention_" + id, userId);
            Notice notice = noticeDao.findNoticeByFormUserIdAndToUserId(id,userId);
            if(notice==null){
                notice = new Notice();
                logger.info("id:"+id);
                Date date = new Date();
                notice.setFormUserId(id);
                notice.setToUserId(userId);
                notice.setCreateTime(date);
                notice.setUpdateTime(date);
                noticeDao.save(notice);
                return Result.success();
            }
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result notNoticeUser(String token, Long userId) {
        logger.info("token: "+token);
        String str = stringRedisTemplate.opsForValue().get(token);
        if (null != str) {
            Long id = Long.parseLong(redisService.getUserId(str));
            User user = userDao.findUserById(userId);
            if (user == null) {
                return Result.failure(ResultCode.FAILURE);
            }
            redisTemplate.opsForSet().remove("attention_" + id, userId);
            noticeDao.removeByFormUserIdAndToUserId(id,userId);
            return Result.success();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result getMyNotice(String token) {
        List<Object> list = new ArrayList<>();
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            Set<Long> ids = redisTemplate.opsForSet().members("attention_" + userId);
            for (Long id : ids) {
                Map<String, Object> map = new HashMap<>();
                User user = userDao.findUserById(id);
                map.put("userId", user.getId());
                map.put("name", user.getName());
                map.put("header", user.getHeader());
                list.add(map);
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getMyData(String token) {
        Map<String,Integer> map = new HashMap<>();
        int comNum = 0;
        int playNum = 0;
        int likeNum = 0;
        int noticeNum = 0;
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str == null) {
          return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<ProCommont> commonts = commontDao.findProCommontsByUserId(userId);
        if (commonts != null) {
            comNum = commonts.size();
        }
        List<ProGood> goods = proGoodDao.findProGoodsByUserId(userId);
        if (goods != null) {
            likeNum = goods.size();
        }
        List<Play> plays = playDao.findPlaysByUserId(userId);
        if(plays!=null){
            playNum = plays.size();
        }
        map.put("comNum",comNum);
        map.put("playNum",playNum);
        map.put("likeNum",likeNum);
        map.put("noticeNum",noticeNum);
        return Result.success(map);
    }

    @Override
    public Result getMyDataByDate(String start, String end, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        List<Object> list = new ArrayList<>();
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<String> dates = getDays(start,end);
        for(String date : dates){
            Map<String,Object> map = new HashMap<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                int comNum = 0;
                int playNum = 0;
                int likeNum = 0;
                int noticeNum = 0;
                Date date1 = dateFormat.parse(date);
                List<Play> plays = playDao.findPlaysByUserIdAndCreateTime(userId,date1);
                if(plays!=null){
                    playNum = plays.size();
                }
                List<ProGood> goods = proGoodDao.findProGoodsByUserIdAndStateAndCreateTime(userId,1,date1);
                if(goods!=null){
                    likeNum = goods.size();
                }
                List<ProCommont> commonts = commontDao.findProCommontsByUserIdAndCreateTime(userId,date1);
                if(commonts!=null){
                    comNum = commonts.size();
                }
                List<Notice> notices = noticeDao.findNoticesByFormUserIdAndCreateTime(userId,date1);
                if(notices!=null){
                    noticeNum = notices.size();
                }
                Map<String,Integer> map1 = new HashMap<>();
                map1.put("comNum",comNum);
                map1.put("playNum",playNum);
                map1.put("likeNum",likeNum);
                map1.put("noticeNum",noticeNum);
                map.put(date,map1);
                list.add(map);
                return Result.success(list);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
       return Result.failure(ResultCode.FAILURE);
    }


    /**
     * 获取两个日期之间的所有日期
     *
     * @param startTime
     *            开始日期
     * @param endTime
     *            结束日期
     * @return
     */
    public  List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }
}
