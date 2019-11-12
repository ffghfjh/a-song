package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.dao.*;
import com.fangzhou.asong.pojo.*;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProducterServiceImpl implements ProducterService {
    Logger logger = LoggerFactory.getLogger(ProducterServiceImpl.class);
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisService redisService;
    @Autowired
    ProductDao productDao;
    @Autowired
    CommontDao commontDao;
    @Autowired
    CommontReplyDao replyDao;
    @Autowired
    UserDao userDao;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    ASongOrderDao orderDao;
    @Autowired
    ProGoodDao proGoodDao;
    @Autowired
    CommontGoodDao commontGoodDao;
    @Autowired
    ReplyGoodDao replyGoodDao;
    @Autowired
    OwnProducterDao ownProducterDao;
    @Autowired
    PlayDao playDao;
    @Autowired
    ShareDao shareDao;
    @Autowired
    NoticeDao noticeDao;
    @Autowired
    SettingDao settingDao;
    @Autowired
    ProductionTypeDao typeDao;


    @Autowired
    ProGoodDao goodDao;
    @Override
    public Result proGood(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            //查询用户对文章的点赞
            ProGood proGood = proGoodDao.findProGoodByProIdAndUserId(proId, userId);
            //没有点过赞
            if (proGood == null) {
                Date date = new Date();
                ProGood good = new ProGood();
                //点赞态
                good.setState(1);
                good.setUserId(userId);
                good.setProId(proId);
                good.setCreateTime(date);
                good.setUpdateTime(date);
                proGoodDao.save(good);
                //添加点赞量到作品表
                logger.info("proId"+proId);
                Product product = productDao.findProductById(proId);
                product.setGoodNum(product.getGoodNum() + 1);
                productDao.save(product);
            }
            //点过赞
            else {
                Product product = productDao.findProductById(proGood.getProId());
                //取消点赞态
                if (proGood.getState() == 0) {
                    //添加点赞量
                    product.setGoodNum(product.getGoodNum() + 1);
                    //改变点赞态
                    proGood.setState(1);
                    proGoodDao.save(proGood);
                }
                //点赞态
                else {
                    //添加点赞量
                    product.setGoodNum(product.getGoodNum() - 1);
                    //改变点赞态
                    proGood.setState(0);
                    proGoodDao.save(proGood);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result proCommont(Long proId, String context, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        //获取评论开启状态
        if(settingDao.findSettingByName("commont").getValue().equals("0")){
            Result result = new Result();
            result.setCode(300);
            result.setMsg("评论功能关闭");
            return result;
        }
        if (str != null && !str.equals("")) {
            Product pro = productDao.findProductById(proId);
            if(pro==null){
                Result result = Result.failure(ResultCode.FAILURE);
                result.setMsg("没有查询到该作品");
                return result;
            }
            Long userId = Long.parseLong(redisService.getUserId(str));
            ProCommont commont = new ProCommont();
            Date date = new Date();
            commont.setProId(proId);
            commont.setUserId(userId);
            commont.setContext(context);
            commont.setCreateTime(date);
            commont.setUpdateTime(date);
            commont.setGoodNum(0);
            ProCommont commont1 = commontDao.save(commont);
            //添加评论量
            pro.setComNum(pro.getComNum() + 1);
            productDao.save(pro);
            return Result.success();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result comReply(int type, Long forId, String context, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            ProComReply reply = new ProComReply();
            Date date = new Date();
            reply.setContext(context);
            reply.setReplyForId(forId);
            reply.setReplyForType(type);
            reply.setUserId(userId);
            reply.setCreateTime(date);
            reply.setUpdateTime(date);
            reply.setGoodNum(0);
            ProComReply reply1 = replyDao.save(reply);
            redisTemplate.opsForSet().add("reply_set", reply1.getId());//存放该评论到
            //设置该回复赞数为0
            redisTemplate.opsForValue().set("reply_" + reply1.getId() + "_" + "counter", 0);
            return Result.success();
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result commontGood(Long commId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            CommontGood commontGood = commontGoodDao.findCommontGoodByComIdAndUserId(commId, userId);

            //没有点过赞
            if (commontGood == null) {
                ProCommont commont = commontDao.findProCommontById(commId);
                commont.setGoodNum(commont.getGoodNum() + 1);
                Date date = new Date();
                CommontGood good = new CommontGood();
                good.setComId(commId);
                good.setState(1);
                good.setUserId(userId);
                good.setCreateTime(date);
                good.setUpdateTime(date);
                commontGoodDao.save(good);
            }
            //点过赞
            else {
                ProCommont commont = commontDao.findProCommontById(commId);
                //取消点赞态
                if (commontGood.getState() == 0) {
                    commont.setGoodNum(commont.getGoodNum() + 1);
                    commontGood.setState(1);
                    commontGoodDao.save(commontGood);
                }
                //点赞态
                else {
                    commont.setGoodNum(commont.getGoodNum() - 1);
                    commontGood.setState(0);
                    commontGoodDao.save(commontGood);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result replyGood(Long replyId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            ProComReply reply = replyDao.findProComReplyById(replyId);
            if (reply != null) {
                Long userId = Long.parseLong(redisService.getUserId(str));
                ReplyGood replyGood = replyGoodDao.findReplyGoodByRepIdAndUserId(replyId, userId);
                if (replyGood == null) {
                    ReplyGood good = new ReplyGood();
                    Date date = new Date();
                    good.setRepId(replyId);
                    good.setUserId(userId);
                    good.setState(1);
                    good.setCreateTime(date);
                    good.setUpdateTime(date);
                    replyGoodDao.save(good);
                    reply.setGoodNum(reply.getGoodNum() + 1);
                } else {
                    if (replyGood.getState() == 0) {
                        reply.setGoodNum(reply.getGoodNum() + 1);
                        replyGood.setState(1);
                        replyGoodDao.save(replyGood);
                    } else {
                        reply.setGoodNum(reply.getGoodNum() - 1);
                        replyGood.setState(0);
                        replyGoodDao.save(replyGood);
                    }
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result getProductCommont(Long proId,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        //获取评论开启状态
        if(settingDao.findSettingByName("commont").getValue().equals("0")){
            Result result = new Result();
            result.setCode(300);
            result.setMsg("评论功能关闭");
            return result;
        }
        Result result;
        List<ProCommont> commonts = commontDao.findProCommontsByProId(proId);
        List<Map<String, Object>> allComs = new ArrayList<>();
        if (commonts != null && commonts.size() > 0) {
            for (ProCommont commont : commonts) {
                Map<String, Object> map = new HashMap<>();
                //评论的用户
                User user = userDao.findUserById(commont.getUserId());
                //评论点赞数
                List<CommontGood> goods = commontGoodDao.findCommontGoodsByComId(commont.getId());
                if(goods!=null){
                    map.put("good", goods.size());
                }else {
                    map.put("good", 0);
                }
                map.put("name",user.getName());
                map.put("userId", user.getId());
                map.put("header", user.getHeader());
                map.put("context", commont.getContext());
                map.put("commId",commont.getId());
                CommontGood good = commontGoodDao.findCommontGoodByComIdAndUserId(commont.getId(),userId);
                if(good==null){
                    map.put("read",false);
                }else {
                    if(good.getState()==0){
                        map.put("read",false);
                    }else {
                        map.put("read",true);
                    }
                }
                List<Map<String, Object>> allReplys = new ArrayList<>();
                //评论的回复
                List<ProComReply> replies = replyDao.findProComRepliesByReplyForIdAndReplyForType(commont.getId(), 1);
                if (replies != null && replies.size() > 0) {
                    for (ProComReply reply : replies) {
                        //回复的点赞数
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("name",user.getName());
                        map1.put("userId", user.getId());
                        map1.put("header", user.getHeader());
                        map1.put("context", reply.getContext());
                        map1.put("good", reply.getGoodNum());
                        map1.put("repId",reply.getId());
                        ReplyGood good1 = replyGoodDao.findReplyGoodByRepIdAndUserId(reply.getId(),userId);
                        if(good1==null){
                            map1.put("read",false);
                        }else {
                            if(good1.getState()==0){
                                map1.put("read",false);
                            }else {
                                map1.put("read",true);
                            }
                        }
                        allReplys.add(map1);
                    }
                }
                map.put("reply", allReplys);
                allComs.add(map);
            }
            return Result.success(allComs);
        } else {
            result = Result.failure(ResultCode.FAILURE);
            result.setMsg("暂无评论");
            return result;
        }
    }

    @Override
    public Result getHotProduct(int count,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Object> list = new ArrayList<>();
        //获取热门作品
        List<Product> products = productDao.findAllByOrderByPayNumAsc();
        if (products != null) {
            if (products.size() > count) {
                for (int i = 0; i < count; i++) {
                    Product pro = products.get(i);
                    Author author = authorDao.findAuthorById(pro.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("productId", pro.getId());
                    map.put("authorId", author.getId());
                    map.put("header", user.getHeader());
                    map.put("title", pro.getTitle());
                    map.put("time", pro.getTime());
                    map.put("date", pro.getCreateTime());
                    map.put("url", pro.getProUrl());
                    map.put("commontSize", pro.getComNum());
                    map.put("play", pro.getPlayNum());
                    map.put("good", pro.getGoodNum());
                    map.put("shareNum",pro.getShareNum());
                    //查询用户是否购买过该作品
                    ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+pro.getId()+"-%", userId, 1);
                    if (order != null) {
                        map.put("state", 1);
                    } else {
                        map.put("state", 0);
                    }
                    List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+pro.getId()+"-%",1);
                    if(orders==null){
                        map.put("payNum",0);
                    }else {
                        map.put("payNum",orders.size());
                    }
                    list.add(map);
                }
            } else {
                for (int i = 0; i < products.size(); i++) {
                    Product pro = products.get(i);
                    Author author = authorDao.findAuthorById(pro.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("productId", pro.getId());
                    map.put("authorId", author.getId());
                    map.put("header", user.getHeader());
                    map.put("title", pro.getTitle());
                    map.put("time", pro.getTime());
                    map.put("date", pro.getCreateTime());
                    map.put("url", pro.getProUrl());
                    map.put("commontSize", pro.getComNum());
                    map.put("play", pro.getPlayNum());
                    map.put("good", pro.getGoodNum());
                    map.put("shareNum",pro.getShareNum());
                    //查询用户是否购买过该作品
                    ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+pro.getId()+"-%", userId, 1);
                    if (order != null) {
                        map.put("state", 1);
                    } else {
                        map.put("state", 0);
                    }
                    List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+pro.getId()+"-%",1);
                    if(orders==null){
                        map.put("payNum",0);
                    }else {
                        map.put("payNum",orders.size());
                    }
                    list.add(map);
                }
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getHotProductsByType(int count,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        Result result;
        List<Object> list = new ArrayList<>();
        //查出点赞排行前20的作品id
        Set<Long> pros = redisTemplate.opsForZSet().range("post_good_rank", 0, 20);
        if (pros != null && pros.size() > 0) {
            for (Long proId : pros) {
                Product pro = productDao.findProductById(proId);
                Author author = authorDao.findAuthorById(pro.getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("productId", pro.getId());
                map.put("authorId", author.getId());
                map.put("header", user.getHeader());
                map.put("title", pro.getTitle());
                map.put("time", pro.getTime());
                map.put("date", pro.getCreateTime());
                map.put("url", pro.getProUrl());
                map.put("shareNum",pro.getShareNum());
                List<ProCommont> commonts = commontDao.findProCommontsByProId(pro.getId());
                //评论数
                if (null != commonts && commonts.size() > 0) {
                    map.put("commontSize", commonts.size());
                } else {
                    map.put("commontSize", 0);
                }
                //播放量
                int play = (int) redisTemplate.opsForValue().get("post_play_" + pro.getId() + "_counter");
                map.put("play", play);
                //作品当前的点赞数
                int goodNum = (int) redisTemplate.opsForValue().get("post_" + proId + "_" + "counter");
                map.put("good", goodNum);
                //查询用户是否购买过该作品
                ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+pro.getId()+"-%", userId, 1);
                if (order != null) {
                    map.put("state", 1);
                } else {
                    map.put("state", 0);
                }
                list.add(map);
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getMyGoodProduct(String token) {
        List<Object> list = new ArrayList<>();
        String str = stringRedisTemplate.opsForValue().get(token);
        if (null != str && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            //查询我所有点赞的作品
            List<ProGood> goods = proGoodDao.findProGoodsByUserIdAndState(userId, 1);
            for (ProGood good : goods) {
                Map<String, Object> map = new HashMap<>();
                map.put("proId", good.getProId());
                Product product = productDao.findProductById(good.getProId());
                Author author = authorDao.findAuthorById(product.getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                map.put("authorId", product.getAuthorId());
                map.put("name",user.getName());
                map.put("userId", user.getId());
                map.put("select",false);
                map.put("productId",product.getId());
                map.put("header", user.getHeader());
                map.put("title", product.getTitle());
                map.put("time", product.getTime());
                map.put("url", product.getProUrl());
                map.put("date", product.getCreateTime());
                map.put("commontSize", product.getComNum());
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
        return Result.success(list);
    }

    @Override
    public Result getLatestProduct(int count,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Object> list = new ArrayList<>();
        List<Product> products = productDao.findAllByOrderByCreateTimeDesc();
        if(products!=null&&products.size()>0){
            if(products.size()>count){
                for(int i=0;i<count;i++){
                    Product product = products.get(i);
                    Author author = authorDao.findAuthorById(product.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", product.getAuthorId());
                    map.put("userId", user.getId());
                    map.put("productId",product.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", product.getTitle());
                    map.put("time", product.getTime());
                    map.put("url", product.getProUrl());
                    map.put("date", product.getCreateTime());
                    map.put("commontSize", product.getComNum());
                    map.put("good", product.getGoodNum());
                    map.put("play", product.getPlayNum());
                    map.put("download", product.getDownNum());
                    map.put("shareNum",product.getShareNum());
                    ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+product.getId()+"-%", userId, 1);
                    if(order==null){
                        logger.info("没有订单信息");
                    }else {
                        logger.info("订单信息:"+order.toString());
                    }

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
            }else{
                for(int i=0;i<products.size();i++){
                    Product product = products.get(i);
                    Author author = authorDao.findAuthorById(product.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", product.getAuthorId());
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", product.getTitle());
                    map.put("time", product.getTime());
                    map.put("productId",product.getId());
                    map.put("url", product.getProUrl());
                    map.put("date", product.getCreateTime());
                    map.put("commontSize", product.getComNum());
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
        return Result.success(list);
    }

    @Override
    public Result getAuthors(int count) {
        List<Object> objs = new ArrayList<>();
        //获取审核通过的作者
        List<Author> authors = authorDao.findAuthorsByState(1);
        if (authors != null && authors.size() > 0) {
            if (authors.size() > count) {
                for (int i = 0; i < count; i++) {
                    Map<String, Object> map = new HashMap<>();
                    Author author = authors.get(i);
                    User user = userDao.findUserById(author.getUserId());
                    map.put("userId", user.getId());
                    map.put("authorId", author.getId());
                    map.put("name", user.getName());
                    map.put("header", user.getHeader());
                    objs.add(map);
                }
            } else {
                for (int i = 0; i < authors.size(); i++) {
                    Map<String, Object> map = new HashMap<>();
                    Author author = authors.get(i);
                    User user = userDao.findUserById(author.getUserId());
                    map.put("userId", user.getId());
                    map.put("authorId", author.getId());
                    map.put("name", user.getName());
                    map.put("header", user.getHeader());
                    objs.add(map);
                }
            }
        }
        return Result.success(objs);
    }

    @Override
    public Result getProducts(int count,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Object> list = new ArrayList<>();
        //获取推荐作品并升序排序
        List<Product> all = productDao.findProductsByRecommendOrderByPayNum(true);
        if (all != null && all.size() > 0) {
            if (all.size() > count) {
                for (int i = 0; i < count; i++) {
                    Author author = authorDao.findAuthorById(all.get(i).getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", all.get(i).getAuthorId());
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", all.get(i).getTitle());
                    map.put("time", all.get(i).getTime());
                    map.put("date", all.get(i).getCreateTime());
                    map.put("url", all.get(i).getProUrl());
                    map.put("download", all.get(i).getDownNum());
                    map.put("play", all.get(i).getPlayNum());
                    map.put("commontSize", all.get(i).getComNum());
                    map.put("good", all.get(i).getGoodNum());
                    map.put("productId",all.get(i).getId());
                    map.put("shareNum",all.get(i).getShareNum());
                    //查询用户是否购买过该作品
                    ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+all.get(i).getId()+"-%", userId, 1);
                    if (order != null) {
                        map.put("state", 1);
                    } else {
                        map.put("state", 0);
                    }
                    List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+all.get(i).getId()+"-%",1);
                    if(orders==null){
                        map.put("payNum",0);
                    }else {
                        map.put("payNum",orders.size());
                    }
                    list.add(map);
                }
            } else {
                for (int i = 0; i < all.size(); i++) {
                    Author author = authorDao.findAuthorById(all.get(i).getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", all.get(i).getAuthorId());
                    map.put("userId", user.getId());
                    map.put("header", user.getHeader());
                    map.put("uname", user.getName());
                    map.put("title", all.get(i).getTitle());
                    map.put("time", all.get(i).getTime());
                    map.put("date", all.get(i).getCreateTime());
                    map.put("url", all.get(i).getProUrl());
                    map.put("download", all.get(i).getDownNum());
                    map.put("play", all.get(i).getPlayNum());
                    map.put("commontSize", all.get(i).getComNum());
                    map.put("good", all.get(i).getGoodNum());
                    map.put("productId",all.get(i).getId());
                    map.put("shareNum",all.get(i).getShareNum());
                    //查询用户是否购买过该作品
                    ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+all.get(i).getId()+"-%", userId, 1);
                    if (order != null) {
                        map.put("state", 1);
                    } else {
                        map.put("state", 0);
                    }
                    List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+all.get(i).getId()+"-%",1);
                    if(orders==null){
                        map.put("payNum",0);
                    }else {
                        map.put("payNum",orders.size());
                    }
                    list.add(map);
                }
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getLatestProductByType(int type,int count,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Object> list = new ArrayList<>();
        List<Product> products = productDao.findProductsByClassIdOrderByCreateTimeDesc(type);
        if(products!=null&&products.size()>0){
            if(products.size()>count){
                for(int i=0;i<count;i++){
                    Product product = new Product();
                    Author author = authorDao.findAuthorById(product.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", product.getAuthorId());
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", product.getTitle());
                    map.put("time", product.getTime());
                    map.put("url", product.getProUrl());
                    map.put("date", product.getCreateTime());
                    map.put("commontSize", product.getComNum());
                    map.put("good", product.getGoodNum());
                    map.put("play", product.getPlayNum());
                    map.put("download", product.getDownNum());
                    map.put("productId",product.getId());
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
            }else {
                for(int i=0;i<products.size();i++){
                    Product product = new Product();
                    Author author = authorDao.findAuthorById(product.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", product.getAuthorId());
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", product.getTitle());
                    map.put("time", product.getTime());
                    map.put("url", product.getProUrl());
                    map.put("date", product.getCreateTime());
                    map.put("commontSize", product.getComNum());
                    map.put("good", product.getGoodNum());
                    map.put("play", product.getPlayNum());
                    map.put("download", product.getDownNum());
                    map.put("productId",product.getId());
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
        return Result.success(list);
    }

    @Override
    public Result getHotAuthors(int count) {
        List<Object> res = new ArrayList<>();
        List<AuthorOwnProduct> list = ownProducterDao.findAuthorOwnProductsByOrderByNumAsc();
        if (list.size() > count) {
            for (int i = 0; i < count; i++) {
                Map<String, Object> map = new HashMap<>();
                AuthorOwnProduct ownProduct = list.get(i);
                Author author = authorDao.findAuthorById(ownProduct.getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                map.put("userId", user.getId());
                map.put("header", user.getHeader());
                map.put("name", user.getName());
                res.add(map);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = new HashMap<>();
                AuthorOwnProduct ownProduct = list.get(i);
                Author author = authorDao.findAuthorById(ownProduct.getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                map.put("userId", user.getId());
                map.put("header", user.getHeader());
                map.put("name", user.getName());
                res.add(map);
            }
        }
        return Result.success(res);
    }

    @Override
    public Result getProjuctsByType(int type,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        //获取分类推荐作品
        List<Product> all = productDao.findProductsByClassIdAndRecommendOrderByPayNum(type,true);
        List<Object> list = new ArrayList<>();
        if (all != null) {
            for (int i = 0; i < all.size(); i++) {
                Author author = authorDao.findAuthorById(all.get(i).getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("authorId", all.get(i).getAuthorId());
                map.put("userId", user.getId());
                map.put("uname", user.getName());
                map.put("header", user.getHeader());
                map.put("title", all.get(i).getTitle());
                map.put("time", all.get(i).getTime());
                map.put("date", all.get(i).getCreateTime());
                map.put("url", all.get(i).getProUrl());
                map.put("download", all.get(i).getDownNum());
                map.put("play", all.get(i).getPlayNum());
                map.put("commontSize", all.get(i).getComNum());
                map.put("good", all.get(i).getGoodNum());
                map.put("productId",all.get(i).getId());
                map.put("shareNum",all.get(i).getShareNum());
                //查询用户是否购买过该作品
                ASongOrder order = orderDao.findASongOrderByProductIdLikeAndUserIdAndState("%-"+all.get(i).getId()+"-%", userId, 1);
                if (order != null) {
                    map.put("state", 1);
                } else {
                    map.put("state", 0);
                }
                List<ASongOrder> orders = orderDao.findASongOrdersByProductIdLikeAndState("%-"+all.get(i).getId()+"-%",1);
                if(orders==null){
                    map.put("payNum",0);
                }else {
                    map.put("payNum",orders.size());
                }
                list.add(map);
            }
        }
        return Result.success(list);
    }

    @Override
    public Result getProductById(Long proId,String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str==null){
            logger.info("token查询redis失败");
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        Product product = productDao.findProductById(proId);
        if(product==null){
            logger.info("未查询到作品");
            return Result.failure(ResultCode.FAILURE);
        }
        if(product!=null){
            Author author = authorDao.findAuthorById(product.getAuthorId());
            User user = userDao.findUserById(author.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("authorId", product.getAuthorId());
            map.put("userId", user.getId());
            map.put("uname", user.getName());
            map.put("header", user.getHeader());
            map.put("title", product.getTitle());
            map.put("time", product.getTime());
            map.put("url", product.getProUrl());
            map.put("date", product.getCreateTime());
            map.put("commontSize", product.getComNum());
            map.put("good", product.getGoodNum());
            map.put("play", product.getPlayNum());
            map.put("download", product.getDownNum());
            map.put("productId",product.getId());
            map.put("shareNum",product.getShareNum());
            ProGood proGood = goodDao.findProGoodByProIdAndUserId(product.getId(),userId);
            if(proGood==null){
                map.put("good",false);
            }else {
                if(proGood.getState()==0){
                    map.put("good",false);
                }else {
                    map.put("good",true);
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
            //当前用户是否关注了作者用户
            Notice notice = noticeDao.findNoticeByFormUserIdAndToUserId(userId,user.getId());
            if(notice==null){
                map.put("notice",false);
            }else {
                map.put("notice",true);
            }

            return Result.success(map);
        }
        return Result.failure(ResultCode.FAILURE);
    }

    @Override
    public Result addPlay(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(null==str){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        Play play = new Play();
        Date date = new Date();
        play.setProId(proId);
        play.setUserId(userId);
        play.setCreateTime(date);
        play.setUpdateTime(date);
        playDao.save(play);
        //次数记录到作品
        Product product = productDao.findProductById(proId);
        product.setPlayNum(product.getPlayNum()+1);
        productDao.save(product);
        return Result.success();
    }

    @Override
    public Result addShare(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(null==str){
            return Result.failure(ResultCode.FAILURE);
        }
        Long userId = Long.parseLong(redisService.getUserId(str));
        Share share = new Share();
        Date date = new Date();
        share.setProId(proId);
        share.setUserId(userId);
        share.setCreateTime(date);
        share.setUpdateTime(date);
        shareDao.save(share);
        //次数记录到作品
        Product product = productDao.findProductById(proId);
        product.setShareNum(product.getShareNum()+1);
        productDao.save(product);
        return Result.success();
    }

    @Override
    public Result searchProduct(String serStr, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if(str == null){
            return Result.failure(ResultCode.FAILURE);
        }
        List<Object> list = new ArrayList<>();
        Long userId = Long.parseLong(redisService.getUserId(str));
        List<Author> authors = authorDao.findAuthorsByStateAndCityLike(1,"%"+serStr+"%");
        if(authors.size()>0){
            for(Author author : authors){
                List<Product> products = productDao.findProductsByAuthorId(author.getId());
                if(products.size()>0){
                    for(Product product : products){
                        User user = userDao.findUserById(author.getUserId());
                        Map<String, Object> map = new HashMap<>();
                        map.put("authorId", product.getAuthorId());
                        map.put("userId", user.getId());
                        map.put("uname", user.getName());
                        map.put("header", user.getHeader());
                        map.put("title", product.getTitle());
                        map.put("time", product.getTime());
                        map.put("date", product.getCreateTime());
                        map.put("url", product.getProUrl());
                        map.put("download", product.getDownNum());
                        map.put("play", product.getPlayNum());
                        map.put("commontSize", product.getComNum());
                        map.put("good", product.getGoodNum());
                        map.put("productId",product.getId());
                        map.put("shareNum",product.getShareNum());
                        //查询用户是否购买过该作品
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
        List<Author> authors1 = authorDao.findAuthorsByStateAndReferralsLike(1,"%"+serStr+"%");
        if(authors1.size()>0){
            for(Author author : authors1){
                List<Product> products = productDao.findProductsByAuthorId(author.getId());
                if(products.size()>0){
                    for(Product product : products){
                        User user = userDao.findUserById(author.getUserId());
                        Map<String, Object> map = new HashMap<>();
                        map.put("authorId", product.getAuthorId());
                        map.put("userId", user.getId());
                        map.put("uname", user.getName());
                        map.put("header", user.getHeader());
                        map.put("title", product.getTitle());
                        map.put("time", product.getTime());
                        map.put("date", product.getCreateTime());
                        map.put("url", product.getProUrl());
                        map.put("download", product.getDownNum());
                        map.put("play", product.getPlayNum());
                        map.put("commontSize", product.getComNum());
                        map.put("good", product.getGoodNum());
                        map.put("productId",product.getId());
                        map.put("shareNum",product.getShareNum());
                        //查询用户是否购买过该作品
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
        List<Author> authors2 = authorDao.findAuthorsByStateAndNameLike(1,"%"+serStr+"%");
        if(authors2.size()>0){
            for(Author author : authors2){
                List<Product> products = productDao.findProductsByAuthorId(author.getId());
                if(products.size()>0){
                    for(Product product : products){
                        User user = userDao.findUserById(author.getUserId());
                        Map<String, Object> map = new HashMap<>();
                        map.put("authorId", product.getAuthorId());
                        map.put("userId", user.getId());
                        map.put("uname", user.getName());
                        map.put("header", user.getHeader());
                        map.put("title", product.getTitle());
                        map.put("time", product.getTime());
                        map.put("date", product.getCreateTime());
                        map.put("url", product.getProUrl());
                        map.put("download", product.getDownNum());
                        map.put("play", product.getPlayNum());
                        map.put("commontSize", product.getComNum());
                        map.put("good", product.getGoodNum());
                        map.put("productId",product.getId());
                        map.put("shareNum",product.getShareNum());
                        //查询用户是否购买过该作品
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
        List<Product> products = productDao.findProductsByTitleLike("%"+serStr+"%");
        if(products.size()>0){
            for(Product product : products){
                Author author = authorDao.findAuthorById(product.getAuthorId());
                User user = userDao.findUserById(author.getUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("authorId", product.getAuthorId());
                map.put("userId", user.getId());
                map.put("uname", user.getName());
                map.put("header", user.getHeader());
                map.put("title", product.getTitle());
                map.put("time", product.getTime());
                map.put("date", product.getCreateTime());
                map.put("url", product.getProUrl());
                map.put("download", product.getDownNum());
                map.put("play", product.getPlayNum());
                map.put("commontSize", product.getComNum());
                map.put("good", product.getGoodNum());
                map.put("productId",product.getId());
                map.put("shareNum",product.getShareNum());
                //查询用户是否购买过该作品
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

        ProductionType type = typeDao.findProductionTypeByName(serStr);
        if(type!=null){
            List<Product> products1 = productDao.findProductsByClassId(type.getId());
            if(products1.size()>0){
                for(Product product : products1){
                    Author author = authorDao.findAuthorById(product.getAuthorId());
                    User user = userDao.findUserById(author.getUserId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("authorId", product.getAuthorId());
                    map.put("userId", user.getId());
                    map.put("uname", user.getName());
                    map.put("header", user.getHeader());
                    map.put("title", product.getTitle());
                    map.put("time", product.getTime());
                    map.put("date", product.getCreateTime());
                    map.put("url", product.getProUrl());
                    map.put("download", product.getDownNum());
                    map.put("play", product.getPlayNum());
                    map.put("commontSize", product.getComNum());
                    map.put("good", product.getGoodNum());
                    map.put("productId",product.getId());
                    map.put("shareNum",product.getShareNum());
                    //查询用户是否购买过该作品
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

        return Result.success(list);
    }
}
