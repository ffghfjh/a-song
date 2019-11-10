package com.fangzhou.asong.controller;

import com.fangzhou.asong.dao.*;
import com.fangzhou.asong.pojo.Author;
import com.fangzhou.asong.pojo.AuthorOwnProduct;
import com.fangzhou.asong.pojo.Product;
import com.fangzhou.asong.pojo.User;
import com.fangzhou.asong.service.FileService;
import com.fangzhou.asong.service.RedisService;
import com.fangzhou.asong.util.JwtTokenUtil;
import com.fangzhou.asong.util.PassToken;
import com.fangzhou.asong.util.Result;
import com.fangzhou.asong.util.UserLoginToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.Cacheable;

import javax.annotation.Resource;
import java.util.Date;

@RestController
public class TestController {
    @Resource
    TestDao testDao;
    @Autowired
    UserDao userDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisService redisService;
    @Autowired
    AuthorDao authorDao;
    @Autowired
    ProductDao productDao;
    @Autowired
    OwnProducterDao ownProducterDao;
    @Autowired
    FileService fileService;

    @GetMapping("/test")
    public String test() {
        String token = JwtTokenUtil.createJWT(1000000000);
        return token;
    }

    @GetMapping("/test1")
    @UserLoginToken
    public String test1(String toekn) {

        return  "测试token验证：success";

    }
    @GetMapping("/priAllPro")
    public String priAllPro() {
        redisService.getProduct();
        return  "success";

    }


    @PostMapping("/testAddUser")
    @Cacheable("user")
    public User testAddUser(String name){

        User user = new User();
        user.setName(name);
        userDao.save(user);
        return user;

    }

    @GetMapping("/testGetUser")
    @Cacheable("user")
    public Result getUser(){
        System.out.println("测试查询用户");
        return Result.success(userDao.findAll());
    }


    @PostMapping("/addUser")
    public Result addUser(String name,String openId){
       User user = new User();
       Date date = new Date();
       user.setName(name);
       user.setOpenid(openId);
       user.setProvince("江西省");
       user.setCity("九江市");
       user.setDistrict("遂川县");
       user.setCreateTime(date);
       user.setUpdateTime(date);
       user.setHeader("group1/M00/00/00/wKiAg13ApEyAO37bAAi22kNoLaI11.jpeg");
       user.setMan(false);
       user.setBirth("1996-02-02");
       user.setPhone("17345589564");
       user.setState(1);
       userDao.save(user);
       return Result.success();
    }

    @PostMapping("/addFile")
    public Result addFile(MultipartFile file){
        return Result.success(fileService.saveFile(file,"mp3"));
    }


    @PostMapping("/addAuthor")
    public Result addAuthor(Long userId,String name){
        User user = userDao.findUserById(userId);
        Date date = new Date();
        Author author = new Author();
        author.setCity(user.getCity());
        author.setState(1);
        author.setCreateTime(date);
        author.setUpdateTime(date);
        author.setProvince(user.getProvince());
        author.setDistrict(user.getDistrict());
        author.setName(name);
        author.setCdCard("454646464");
        author.setCardUrl("654545");
        author.setUserId(userId);
        authorDao.save(author);
        user.setState(1);
        userDao.save(user);
        return Result.success();
    }


    @PostMapping("/addProject")
    public Result addProject(Long authorId,String name){
        Author author = authorDao.findAuthorById(authorId);
        Product product = new Product();
        Date date = new Date();
        product.setAuthorId(author.getId());
        product.setClassId(1);
        product.setState(Product.ONSHELF);
        product.setProUrl("45445565654654654");
        product.setTitle(name);
        product.setCreateTime(date);
        product.setUpdateTime(date);
        product.setGoodNum(0);
        product.setPlayNum(0);
        product.setDownNum(0);
        product.setComNum(0);
        Product product1 = productDao.save(product);

        //记录作者贡献的作品数
        AuthorOwnProduct ownProduct = ownProducterDao.findAuthorOwnProductByAuthorId(author.getId());
        if(ownProduct==null){
            AuthorOwnProduct ownProduct1 = new AuthorOwnProduct();
            ownProduct1.setAuthorId(author.getId());
            ownProduct1.setNum(1);
            ownProduct1.setCreateTime(date);
            ownProduct1.setUpdateTime(date);
            ownProducterDao.save(ownProduct1);
        }else{
            ownProduct.setNum(ownProduct.getNum()+1);
            ownProducterDao.save(ownProduct);
        }
        return Result.success();
    }
}
