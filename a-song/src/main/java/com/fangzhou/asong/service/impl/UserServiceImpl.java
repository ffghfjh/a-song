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
import org.aspectj.weaver.ast.Or;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    @Autowired
    MessageDao messageDao;

    @Autowired
    ASongOrderDao orderDao;

    @Autowired
    ReferralsDao referralsDao;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${wxchat.appid}")
    String appid;
    @Value("${wxchat.secret}")
    String secret;
    @Value("${wxchat.key}")
    String key;


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
                stringRedisTemplate.opsForValue().set(token, openId + "%" + sessionKey + "%" + user.getId());
            }
            //新用户
            else {
                logger.info(appid + "," + encryptedData + "," + sessionKey + "," + iv);
                String userInfo = WxCore.decrypt(appid, encryptedData, sessionKey, iv);
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
                    newUser.setState(1);
                    newUser.setAuthor(false);
                    User user1 = userDao.save(newUser);
                    map.put("userId", user1.getId());
                    map.put("header", user1.getHeader());
                    map.put("name", user1.getName());
                    stringRedisTemplate.opsForValue().set(token, openId + "%" + sessionKey + "%" + user1.getId());
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
            logger.info("sessionKey:"+redisService.getSessionKey(str));
            logger.info("userId:"+redisService.getUserId(str));
            Long userId = Long.parseLong(redisService.getUserId(str));
            User user = userDao.findUserById(userId);
            map.put("header", user.getHeader());
            map.put("name", user.getName());
            map.put("userId",user.getId());
            map.put("name",user.getName());
            map.put("man",user.isMan());
            map.put("city",user.getCity());
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
    public Result releaseProductNoAu(String name, int type,String time, MultipartFile file, String token,
                                  String prov,String city, String reference, int age) {

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
                        author.setProvince(prov);
                        author.setName(user.getName());
                        author.setCreateTime(date);
                        author.setUpdateTime(date);

                        //推荐人信息
                        if(reference!=null){
                            author.setReferrals(reference);
                            Referrals referrals =  referralsDao.findReferralsByName(reference);
                            if(referrals==null){
                                Referrals referrals1 = new Referrals();
                                referrals1.setName(reference);
                                referrals1.setNum(1);
                                referrals1.setCreateTime(date);
                                referrals1.setUpdateTime(date);
                                referralsDao.save(referrals1);
                            }else {
                                referrals.setNum(referrals.getNum()+1);
                                referralsDao.save(referrals);
                            }
                        }
                        authorDao.save(author);
                        user.setAuthor(true);
                        userDao.save(user);

                        String url = fileService.saveFile(file, file.getContentType().split("/")[1]);//作品地址
                        Product product = new Product();
                        product.setAuthorId(author.getId());
                        product.setClassId(type);
                        product.setState(Product.ONSHELF);
                        product.setProUrl(url);
                        product.setTitle(name);
                        product.setTime(time);
                        product.setCreateTime(date);
                        product.setUpdateTime(date);
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

                        SongMessage message = new SongMessage();
                        message.setType(1);//作品发布成功消息
                        message.setUserId(user.getId());
                        message.setCreateTime(date);
                        message.setUpdateTime(date);
                        message.setIsread(false);
                        messageDao.save(message);
                        logger.info("发布作品成功");
                        //redisService.addProduct(product1.getId());
                        return Result.success();
                    }else{
                        return Result.failure(ResultCode.INTERFACE_FORBID_VISIT);
                    }
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
    public Result releaseProduct(String name, int type,String time, MultipartFile file, String token) {
        Result result;
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        User user = userDao.findUserById(userId);
        if(user!=null){
            if (user.getState() == 1) {
               if(user.isAuthor()){
                 Author author = authorDao.findAuthorByUserIdAndState(user.getId(),1);
                 if(author!=null){
                     Date date = new Date();
                     String url = fileService.saveFile(file, file.getContentType().split("/")[1]);//作品地址
                     Product product = new Product();
                     product.setAuthorId(author.getId());
                     product.setClassId(type);
                     product.setState(Product.ONSHELF);
                     product.setProUrl(url);
                     product.setTitle(name);
                     product.setTime(time);
                     product.setCreateTime(date);
                     product.setUpdateTime(date);
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
                     //添加消息到消息
                     SongMessage message = new SongMessage();
                     message.setType(1);//作品发布成功消息
                     message.setUserId(user.getId());
                     message.setCreateTime(date);
                     message.setUpdateTime(date);
                     message.setIsread(false);
                     messageDao.save(message);
                     //redisService.addProduct(product1.getId());
                     logger.info("发布作品成功");
                     return Result.success();
                 }
                 logger.error("author is null");
                 return Result.failure(ResultCode.FAILURE);
               }
                result = Result.failure(ResultCode.FAILURE);
                result.setMsg("非作者");
                return result;
            }
            result = Result.failure(ResultCode.FAILURE);
            result.setMsg("被封号");
            return result;
        }
        return null;
    }

    @Override
    public Result noticeUser(String token, Long userId) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (null != str) {
            Long id = Long.parseLong(redisService.getUserId(str));
           logger.info("关注的用户ID："+id);
            logger.info("id:"+id);
            User user = userDao.findUserById(userId);

            if (user == null) {
                logger.info("关注失败，user is null");
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

                SongMessage message = new SongMessage();
                message.setType(2);//关注消息
                message.setUserId(userId);
                message.setCreateTime(date);
                message.setUpdateTime(date);
                message.setIsread(false);
                messageDao.save(message);
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
        Map<String,Object> allMap = new HashMap<>();
        List<Object> timeList = new ArrayList<>();
        List<Object> playList = new ArrayList<>();
        List<Object> comList = new ArrayList<>();
        List<Object> likeList = new ArrayList<>();
        List<Object> noticeList = new ArrayList<>();

        List<Object> list = new ArrayList<>();
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<String> dates = getDays(start,end);
        for(String date : dates){
            Map<String,Object> map = new HashMap<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                int comNum = 0;
                int playNum = 0;
                int likeNum = 0;
                int noticeNum = 0;
            Date date1 = null;
            try {
                date1 = dateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
                playList.add(playNum);
                noticeList.add(noticeNum);
                comList.add(comNum);
                likeList.add(likeNum);
                timeList.add(date);;
        }
        allMap.put("date",timeList);
        allMap.put("play",playList);
        allMap.put("comm",comList);
        allMap.put("like",likeList);
        allMap.put("notice",noticeList);
       return Result.success(allMap);
    }

    @Override
    public Result getIsAuthor(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        User user = userDao.findUserById(userId);
        if(user.isAuthor()){
            return Result.success(true);
        }
        return Result.success(false);
    }

    @Override
    public Result getMessages(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<SongMessage> messages = messageDao.findMessagesByUserIdAndAndIsread(userId,false);
        if(messages!=null){
            for(SongMessage message : messages){
                message.setIsread(true);
                messageDao.save(message);
            }
            return Result.success(messages);
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public void payResult(HttpServletRequest request, HttpServletResponse response) {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            //sb为微信返回的xml
            String notityXml = sb.toString();
            String resXml = "";
            System.out.println("接收到的报文：" + notityXml);
            Map<String,String> map = WxPayUtil.xmlToMap(notityXml);
            String returnCode = map.get("return_code");
            if("SUCCESS".equals(returnCode)){
                //验证签名是否正确
                //Map<String, String> validParams = PayUtil.paraFilter(map);  //回调验签时需要去除sign和空值参数
                //String validStr = PayUtil.createLinkString(validParams);//把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
                String sign = WxPayUtil.getSign(map,key);
                // 因为微信回调会有八次之多,所以当第一次回调成功了,那么我们就不再执行逻辑了

                //根据微信官网的介绍，此处不仅对回调的参数进行验签，还需要对返回的金额与系统订单的金额进行比对等
                if(sign.equals(map.get("sign"))){
                    /**此处添加自己的业务逻辑代码start**/
                    String orderNum = map.get("out_trade_no");
                    ASongOrder order = orderDao.findASongOrderByOrderNum(orderNum);
                    if(order!=null){
                        if(order.getState()==0){
                            order.setState(1);
                            orderDao.save(order);
                        }
                    }
                    /**此处添加自己的业务逻辑代码end**/
                    //通知微信服务器已经支付成功
                    resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                            + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                } else {
                    System.out.println("微信支付回调失败!签名不一致");
                }
                System.out.println(resXml);
                System.out.println("微信支付回调数据结束");

                BufferedOutputStream out = new BufferedOutputStream(
                        response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result getMyNoticeProduct(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Notice> notices = noticeDao.findNoticesByFormUserId(userId);
        if(notices==null){
            return Result.success();
        }
        List<Object> list = new ArrayList<>();
        for(Notice notice : notices){
            Long toUserId = notice.getToUserId();
            User user = userDao.findUserById(toUserId);
            if(user!=null){
                if(user.isAuthor()){
                    Author author = authorDao.findAuthorByUserIdAndState(toUserId,1);
                    if(author!=null){
                        List<Product> products = productDao.findProductsByAuthorId(author.getId());
                        for(Product product : products){
                            Map<String, Object> map = new HashMap<>();
                            map.put("select",false);
                            map.put("authorId", product.getAuthorId());
                            map.put("userId", user.getId());
                            map.put("productId",product.getId());
                            map.put("name", user.getName());
                            map.put("header", user.getHeader());
                            map.put("title", product.getTitle());
                            map.put("time", product.getTime());
                            map.put("url", product.getProUrl());
                            map.put("date", product.getCreateTime());
                            map.put("commontSize", product.getPlayNum());
                            map.put("good", product.getGoodNum());
                            map.put("play", product.getPlayNum());
                            map.put("download", product.getDownNum());
                            map.put("shareNum",product.getShareNum());
                            ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+product.getId()+"-%", userId, 1);
                            if (order != null) {
                                map.put("state", 1);
                            } else {
                                map.put("state", 0);
                            }
                            List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+product.getId()+"-%",1);
                            if(orders==null){
                                map.put("payNum",0);
                            }else {
                                map.put("payNum",orders.size());
                            }
                            list.add(map);
                        }

                    }
                }
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getMyBuyProduct(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<ASongOrder> orders = orderDao.findASongOrdersByUserIdAndState(userId,1);
        List<Object> list = new ArrayList<>();
        if(orders!=null){
            for(ASongOrder order : orders){
                String proIds = order.getProductId();
                String[] ids = proIds.split("-");
                if(ids!=null){
                    for(String id : ids){
                        if(!id.equals("")){
                            Product product = productDao.findProductById(Long.parseLong(id));
                            Author author = authorDao.findAuthorById(product.getAuthorId());
                            User user = userDao.findUserById(author.getUserId());
                            if(product!=null){
                                Map<String, Object> map = new HashMap<>();
                                map.put("select",false);
                                map.put("authorId", product.getAuthorId());
                                map.put("userId", user.getId());
                                map.put("productId",product.getId());
                                map.put("uname", user.getName());
                                map.put("header", user.getHeader());
                                map.put("title", product.getTitle());
                                map.put("time", product.getTime());
                                map.put("url", product.getProUrl());
                                map.put("date", product.getCreateTime());
                                map.put("commontSize", product.getPlayNum());
                                map.put("good", product.getGoodNum());
                                map.put("play", product.getPlayNum());
                                map.put("download", product.getDownNum());
                                map.put("shareNum",product.getShareNum());
                                map.put("state", 1);
                                List<ASongOrder> orders1 = orderDao.findASongOrdersByProductIdLikeAndState("%-"+product.getId()+"-%",1);
                                if(orders1==null){
                                    map.put("payNum",0);
                                }else {
                                    map.put("payNum",orders.size());
                                }
                                list.add(list);
                            }
                        }
                    }
                }

            }
        }
        return Result.success(list);
    }

    @Override
    public Result getMyPlayProduct(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Play> plays = playDao.findPlaysByUserId(userId);
        if(plays!=null){
            List<Object> list = new ArrayList<>();
            for(Play play : plays){
              Product product = productDao.findProductById(play.getProId());
              if(product!=null){
                  Author author = authorDao.findAuthorById(product.getAuthorId());
                  User user = userDao.findUserById(author.getUserId());
                  Map<String, Object> map = new HashMap<>();
                  map.put("select",false);
                  map.put("authorId", product.getAuthorId());
                  map.put("userId", user.getId());
                  map.put("productId",product.getId());
                  map.put("uname", user.getName());
                  map.put("header", user.getHeader());
                  map.put("title", product.getTitle());
                  map.put("time", product.getTime());
                  map.put("url", product.getProUrl());
                  map.put("date", product.getCreateTime());
                  map.put("commontSize", product.getPlayNum());
                  map.put("good", product.getGoodNum());
                  map.put("play", product.getPlayNum());
                  map.put("download", product.getDownNum());
                  map.put("shareNum",product.getShareNum());
                  ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+product.getId()+"-%", userId, 1);
                  if (order != null) {
                      map.put("state", 1);
                  } else {
                      map.put("state", 0);
                  }
                  List<ASongOrder> orders1 = orderDao.findASongOrdersByProductIdLikeAndState("%-"+product.getId()+"-%",1);
                  if(orders1==null){
                      map.put("payNum",0);
                  }else {
                      map.put("payNum",orders1.size());
                  }
                  list.add(map);
              }
            }
            return Result.success(list);
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result getMyOrderInfo(String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(null == str){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<ASongOrder> orders = orderDao.findASongOrdersByUserId(userId);
        List<Object> list = new ArrayList<>();
        if(orders==null){
            return Result.success(list);
        }
        User user = userDao.findUserById(userId);
        for(ASongOrder order : orders){
            Map<String,Object> map = new HashMap<>();
            String[] strs = order.getProductId().split("-");
            if(strs!=null&&strs.length>0){
                for(String st : strs){
                    if(!st.equals("")&&st!=null){
                        Long id = Long.parseLong(st);
                        Product product = productDao.findProductById(id);
                        if(product!=null){
                            map.put("productId",id);
                            map.put("title",product.getTitle());
                            map.put("userId",userId);
                            map.put("name",user.getName());
                            map.put("header",user.getHeader());
                            map.put("money",order.getMoney());
                            map.put("state",order.getState());
                            map.put("time",order.getCreateTime());
                            list.add(map);
                        }
                    }
                }
            }

        }
        return Result.success(list);
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
