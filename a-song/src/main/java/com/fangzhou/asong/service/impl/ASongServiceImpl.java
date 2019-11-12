package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.bean.OrderInfo;
import com.fangzhou.asong.dao.*;
import com.fangzhou.asong.pojo.*;
import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.*;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@CacheConfig(cacheNames = "aSongService")
public class ASongServiceImpl implements ASongService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisService redisService;
    @Autowired
    FeedBackDao backDao;
    @Autowired
    ProductClassDao classDao;
    @Autowired
    CommontDao commontDao;
    @Autowired
    ProGoodDao goodDao;
    @Autowired
    ASongOrderDao orderDao;
    @Autowired
    FileService fileService;
    @Autowired
    ProductDao productDao;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    AdverisingDao adverisingDao;
    @Autowired
    SettingDao settingDao;
    @Autowired
    UserDao userDao;


    @Value("${wxchat.appid}")
    String appId;
    @Value("${wxchat.secret}")
    String secret;
    @Value("${wxchat.mch_id}")
    String mchId;
    @Value("${ip}")
    String ip;
    @Value("${noticeUrl}")
    String noticeUrl;
    @Value("${wxchat.key}")
    String key;
    @Value("${product.price}")
    float price;
    String payUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";


    Logger logger = LoggerFactory.getLogger(ASongServiceImpl.class);

    @Override
    @Cacheable(value = "getProClass")
    public Result getProClass() {
        Iterable<ProductionType> types = classDao.findAll();
        return Result.success(types);
    }


    @Override
    public Result addFaceBack(String context, String name, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            FeedBack back = new FeedBack();
            Date date = new Date();
            back.setContext(context);
            back.setName(name);
            back.setUserId(userId);
            back.setCreateTime(date);
            back.setUpdateTime(date);
            backDao.save(back);
            return Result.success();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result getMyASongData(String token) {
        Result result;
        Map<String, Object> map = new HashMap<>();
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            //我的总评论量
            int sumCommont = 0;
            Long userId = Long.parseLong(redisService.getUserId(str));
            List<ProCommont> commonts = commontDao.findProCommontsByUserId(userId);
            if (commonts != null && commonts.size() > 0) {
                sumCommont = commonts.size();
            }
            map.put("commont", sumCommont);
            //我的总点赞量
            int sumGood = 0;
            List<ProGood> goods = goodDao.findProGoodsByUserId(userId);
            if (goods != null && goods.size() > 0) {
                sumGood = goods.size();
            }
            map.put("good", sumGood);

            //我的播放量
            int playNum = redisTemplate.opsForList().range("play_" + userId, 0, -1).size();
            map.put("play", playNum);
            //我的下载量
            int downloadNum = redisTemplate.opsForList().range("download_" + userId, 0, -1).size();
            map.put("download", downloadNum);

        }
        return null;
    }

    @Override
    public Result addPlay(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (null != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            ProductPlay productPlay = new ProductPlay();
            Date date = new Date();
            productPlay.setProId(proId);
            productPlay.setUserId(userId);
            productPlay.setUpdateTime(date);
            productPlay.setCreateTime(date);
            //添加记录到用户播放list中
            redisTemplate.opsForList().leftPush("play_" + userId, productPlay);
        }
        return Result.success();
    }

    @Override
    public Result downLoadProduct(Long proId, String token, HttpServletResponse response) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            //查询是否购买了此作品
            ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+proId+"-%", userId, 1);
            if (order != null) {
                Product product = productDao.findProductById(proId);
//                byte[] bytes = fileService.downloadFile(product.getProUrl());
//                // 这里只是为了整合fastdfs，所以写死了文件格式。需要在上传的时候保存文件名。下载的时候使用对应的格式
//                try {
//                    response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(new RandomString().nextString()+".mp3", "UTF-8"));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                response.setCharacterEncoding("UTF-8");
//                ServletOutputStream outputStream = null;
//                try {
//                    outputStream = response.getOutputStream();
//                    outputStream.write(bytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        outputStream.flush();
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                //添加下载次数
                product.setDownNum(product.getDownNum()+1);
                return Result.success();
            }
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result getHotAuthors() {

        List<Author> authors = authorDao.findAuthorsByState(1);
        if (null != authors && authors.size() > 0) {
            for (Author author : authors) {
                List<Product> products = productDao.findProductsByAuthorId(author.getId());
                if (products != null) {

                }
            }
        }
        return null;
    }

    @Override
    public Result getAdvertising() {
        Map<String, Object> map = new HashMap<>();
        List<Advertising> advertisings = adverisingDao.findAdvertisingByType(1);
        map.put("banner", advertisings);
        List<Advertising> advertisings1 = adverisingDao.findAdvertisingByType(2);
        map.put("operation1", advertisings1);
        List<Advertising> advertisings2 = adverisingDao.findAdvertisingByType(3);
        map.put("operation2", advertisings2);
        return Result.success(map);
    }

    @Override
    public Result getOrder(String token, List<Long> proIds) {
        Result result;
        for(Long proId : proIds){
            Product product = productDao.findProductById(proId);
            if (product == null) {
                result = Result.failure(ResultCode.FAILURE);
                result.setMsg("不存在此商品");
                return result;
            }
        }
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str == null) {
            result = Result.failure(ResultCode.FAILURE);
            result.setMsg("token不存在");
            return result;
        }

        Long userId = Long.parseLong(redisService.getUserId(str));
        User user = userDao.findUserById(userId);

        for(int i =0;i<proIds.size();i++){
            //查询是否已经购买过
            ASongOrder order2 = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+proIds.get(i)+"-%",userId,1);
            if(order2!=null){
                logger.info("购买过Id为"+proIds.get(i)+"的商品");
                proIds.remove(i);
            }else{
                logger.info("未购买过Id为"+proIds.get(i)+"的商品");
            }
        }
        //商户订单号
        String outTradeNo = getOrderNo();
        OrderInfo order = new OrderInfo();
        order.setAppid(appId);
        order.setMch_id(mchId);
        String nonceStr = RandomStringGenerator.getRandomStringByLength(32);
        logger.info(nonceStr);
        order.setNonce_str(nonceStr);
        order.setBody("asong");
        order.setOut_trade_no(outTradeNo);
        //int money = (int) (price*100)*proIds.size();
        int money = 1;
        order.setTotal_fee(money);
        order.setNotify_url(noticeUrl);
        order.setSpbill_create_ip(ip);
        order.setTrade_type("JSAPI");
        order.setOpenid(user.getOpenid());
        logger.info("用户下单openId:"+user.getOpenid());
        //生成签名
        try {
            Map<String,String> map = new HashMap<>();
            map.put("appid",order.getAppid());
            map.put("mch_id",order.getMch_id());
            map.put("nonce_str",order.getNonce_str());
            map.put("body",order.getBody());
            map.put("out_trade_no",order.getOut_trade_no());
            map.put("total_fee",order.getTotal_fee()+"");
            map.put("spbill_create_ip",order.getSpbill_create_ip());
            map.put("notify_url",order.getNotify_url());
            map.put("trade_type",order.getTrade_type());
            map.put("openid",user.getOpenid());
            String sign = WxPayUtil.getSign(map,key);
            map.put("sign",sign);
            logger.info("签名："+sign);
            order.setSign(sign);
            String result1 = WxPayUtil.sendPost(map,payUrl,1000000,100000);
            logger.info("下单返回参数"+result1);logger.info("下单返回参数"+result1);
            Map<String,String> resultMap = WxPayUtil.xmlToMap(result1);
            logger.info("解析完毕："+resultMap.toString());
            System.out.println(resultMap.get("result_code")+","+resultMap.get("return_code"));
            // 二次签名
            if ("SUCCESS".equals(resultMap.get("result_code")) && resultMap.get("return_code").equals(resultMap.get("result_code"))) {
                Map<String,String> map1 = new HashMap<>();
                map1.put("appId",appId);
                long time = System.currentTimeMillis()/1000;
                map1.put("timeStamp",String.valueOf(time));
                map1.put("nonceStr",RandomStringGenerator.getRandomStringByLength(32));
                map1.put("package","prepay_id="+resultMap.get("prepay_id"));
                map1.put("signType","MD5");
                //生成签名
                String sign1 = WxPayUtil.getSign(map1,key);
                Map payInfo = new HashMap();
                payInfo.put("timeStamp", map1.get("timeStamp"));
                payInfo.put("nonceStr", map1.get("nonceStr"));
                payInfo.put("package", map1.get("package"));
                payInfo.put("signType", map1.get("signType"));
                payInfo.put("paySign", sign1);


                String productId = "-";
                if(proIds.size()>0){
                    for(Long proId : proIds){
                        logger.info("订单产品id:"+proId);
                        productId=productId+proId+"-";
                    }
                    //统一下单业务
                    ASongOrder order1 = new ASongOrder();
                    Date date = new Date();
                    order1.setUserId(userId);
                    order1.setMoney(money);
                    order1.setOrderNum(outTradeNo);
                    order1.setProductId(productId);
                    order1.setState(0);
                    order1.setCreateTime(date);
                    order1.setUpdateTime(date);
                    orderDao.save(order1);
                }
                return Result.success(payInfo);
            }

        } catch (IllegalAccessException e) {
            logger.error("签名生成异常");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }  catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result getUsersProduct(Long authorId,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
            List<Object> list = new ArrayList<>();
            Author author = authorDao.findAuthorById(authorId);
            //查询作者
            if (author != null) {
                User user = userDao.findUserById(author.getUserId());
                List<Product> products = productDao.findProductsByAuthorId(author.getId());
                if (products != null && products.size() > 0) {
                    for(Product product : products){
                        Map<String, Object> map = new HashMap<>();
                        map.put("authorId", product.getAuthorId());
                        map.put("userId", user.getId());
                        map.put("name", user.getName());
                        map.put("header", user.getHeader());
                        map.put("title", product.getTitle());
                        map.put("time", product.getTime());
                        map.put("url", product.getProUrl());
                        map.put("date", product.getCreateTime());
                        map.put("commontSize", product.getComNum());
                        map.put("goodNum", product.getGoodNum());
                        map.put("play", product.getPlayNum());
                        map.put("download", product.getDownNum());
                        map.put("productId", product.getId());
                        map.put("shareNum",product.getShareNum());
                        ProGood proGood = goodDao.findProGoodByProIdAndUserId(product.getId(), user.getId());
                        if (proGood == null) {
                            map.put("good", false);
                        } else {
                            if (proGood.getState() == 0) {
                                map.put("good", false);
                            } else {
                                map.put("good", true);
                            }
                        }
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
                    return Result.success(list);
            }
        }
        return Result.failure(ResultCode.FAILURE);
    }


    public String getRandomStringByLength(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * @return String
     * @function 生成商户订单号/退款单号
     * @date 2015-12-17
     */
    public String getOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = new Date();
        return sdf.format(date) + getRandomStringByLength(4);
    }
}
