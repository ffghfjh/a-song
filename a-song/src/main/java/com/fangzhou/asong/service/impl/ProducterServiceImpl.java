package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.dao.CommontDao;
import com.fangzhou.asong.dao.CommontReplyDao;
import com.fangzhou.asong.dao.ProductDao;
import com.fangzhou.asong.dao.UserDao;
import com.fangzhou.asong.pojo.ProComReply;
import com.fangzhou.asong.pojo.ProCommont;
import com.fangzhou.asong.pojo.Product;
import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.ProducterService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.ResultCode;
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

    @Override
    public Result proGood(Long proId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        int userId = Integer.parseInt(redisService.getUserId(str));
        //文章当前的点赞数
        int goodNum = (int) redisTemplate.opsForValue().get("post_" + proId + "_" + "counter");
        //获取用户对当前文章的点赞状态
        Boolean good = (Boolean) redisTemplate.opsForHash().get("post_like_" + proId + "_" + userId, "state");
        //没有点过赞
        if (good == null) {
            logger.info("用户：" + userId + "没有对文章：" + proId + "点过赞");
            //存放作品id到作品set中
            redisTemplate.opsForSet().add("post_set", proId);
            //存放userID到作品点赞set中
            redisTemplate.opsForSet().add("post_user_like_set_" + proId, userId);
            redisTemplate.opsForValue().set("post_" + proId + "_" + "counter", goodNum + 1);
            //储存作品的点赞情况到文章点赞有序集合上
            redisTemplate.opsForZSet().add("post_good_rank", proId, goodNum + 1);
            redisTemplate.opsForHash().put("post_like_" + proId + "_" + userId, "state", true);
        } else {
            logger.info("用户：" + userId + "对文章：" + proId + "点过赞");
            //取消赞状态
            if (!good) {
                logger.info("用户：" + userId + "对文章：" + proId + "点赞");
                //增加点赞数
                redisTemplate.opsForValue().set("post_" + proId + "_" + "counter", goodNum + 1);
                //储存作品的点赞情况到文章点赞有序集合上
                redisTemplate.opsForZSet().add("post_good_rank", proId, goodNum + 1);
                //更改点赞状态
                redisTemplate.opsForHash().put("post_like_" + proId + "_" + userId, "state", true);
            } else {
                logger.info("用户：" + userId + "对文章：" + proId + "取消点赞");
                //减少点赞数
                redisTemplate.opsForValue().set("post_" + proId + "_" + "counter", goodNum - 1);
                //储存作品的点赞情况到文章点赞有序集合上
                redisTemplate.opsForZSet().add("post_good_rank", proId, goodNum - 1);
                //更改点赞状态
                redisTemplate.opsForHash().put("post_like_" + proId + "_" + userId, "state", false);
            }
        }
        return Result.success();
    }

    @Override
    public Result proCommont(Long proId, String context, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            ProCommont commont = new ProCommont();
            Date date = new Date();
            commont.setProId(proId);
            commont.setUserId(userId);
            commont.setContext(context);
            commont.setCreateTime(date);
            commont.setUpdateTime(date);
            ProCommont commont1 = commontDao.save(commont);
            //存放评论ID到评论set集合中
            redisTemplate.opsForSet().add("commont_set", commont1.getId());
            //设置该评论赞数为0
            redisTemplate.opsForValue().set("commont_" + commont1.getId() + "_" + "counter", 0);
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
            int goodNum = (int) redisTemplate.opsForValue().get("commont_" + commId + "_" + "counter");
            //查询该用户是否对该评论点过赞
            Boolean good = (Boolean) redisTemplate.opsForHash().get("commont_like_" + commId + "_" + userId, "state");
            //没有点过赞
            if (good == null) {
                //设置点赞数为1
                redisTemplate.opsForValue().set("commont_" + commId + "_" + "counter", 1);
                //设置状态为点赞态
                redisTemplate.opsForHash().put("commont_like_" + commId + "_" + userId, "state", true);
            }
            //点过赞
            else {
                //取消点赞态
                if (!good) {
                    //设置点赞数加1
                    redisTemplate.opsForValue().set("commont_" + commId + "_" + "counter", goodNum + 1);
                    //设置状态为点赞态
                    redisTemplate.opsForHash().put("commont_like_" + commId + "_" + userId, "state", true);
                }
                //点赞态
                else {
                    //设置点赞数减1
                    redisTemplate.opsForValue().set("commont_" + commId + "_" + "counter", goodNum - 1);
                    //设置状态为取消点赞态
                    redisTemplate.opsForHash().put("commont_like_" + commId + "_" + userId, "state", false);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result replyGood(Long replyId, String token) {
        String str = stringRedisTemplate.opsForValue().get(token);
        if (str != null && !str.equals("")) {
            Long userId = Long.parseLong(redisService.getUserId(str));
            //获取当前点赞数
            int goodNum = (int) redisTemplate.opsForValue().get("reply" + replyId + "_" + "counter");
            //查询点赞状态
            Boolean good = (Boolean) redisTemplate.opsForHash().get("reply_like_" + replyId + "_" + userId, "state");
            //没有点过赞
            if (good == null) {
                redisTemplate.opsForValue().set("reply" + replyId + "_" + "counter", 1);
                redisTemplate.opsForHash().put("reply_like_" + replyId + "_" + userId, "state", true);
            }
            //点过赞
            else {
                //取消点赞态
                if (!good) {
                    redisTemplate.opsForValue().set("reply" + replyId + "_" + "counter", goodNum + 1);
                    redisTemplate.opsForHash().put("reply_like_" + replyId + "_" + userId, "state", true);
                }
                //点赞态
                else {
                    redisTemplate.opsForValue().set("reply" + replyId + "_" + "counter", goodNum - 1);
                    redisTemplate.opsForHash().put("reply_like_" + replyId + "_" + userId, "state", false);
                }
            }
        }
        return Result.success();
    }

    @Override
    public Result getProductCommont(Long proId) {
        Result result;
        List<ProCommont> commonts = commontDao.findProCommontsByProId(proId);
        List<Map<String, Object>> allComs = new ArrayList<>();
        if (commonts != null && commonts.size() > 0) {
            for (ProCommont commont : commonts) {
                Map<String, Object> map = new HashMap<>();
                //评论的用户
                User user = userDao.findUserById(commont.getUserId());
                //评论点赞数
                int goodNum = (int) redisTemplate.opsForValue().get("commont_" + commont.getId() + "_" + "counter");
                map.put("userId", user.getId());
                map.put("header", user.getHeader());
                map.put("context", commont.getContext());
                map.put("good", goodNum);
                List<Map<String, Object>> allReplys = new ArrayList<>();
                //评论的回复
                List<ProComReply> replies = replyDao.findProComRepliesByReplyForIdAndReplyForType(commont.getId(), 1);
                if (replies != null && replies.size() > 0) {
                    for (ProComReply reply : replies) {
                        //回复的用户
                        User user1 = userDao.findUserById(reply.getUserId());
                        //回复的点赞数
                        int goodNum1 = (int) redisTemplate.opsForValue().get("reply_" + reply.getId() + "_" + "counter");
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("userId", user.getId());
                        map1.put("header", user.getHeader());
                        map1.put("context", reply.getContext());
                        map1.put("good", goodNum1);
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
    public Result getProduct() {
        Result result;
        List<Object> list = new ArrayList<>();
        //查出点赞排行前20的作品id
        Set<Long> pros = redisTemplate.opsForZSet().range("post_good_rank", 0, 20);
        if (pros != null && pros.size() > 0) {
            for (Long proId : pros) {
                Product pro = productDao.findProductById(proId);
                list.add(pro);
            }
        }
        return Result.success(list);
    }
}
