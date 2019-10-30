package com.fangzhou.asong.controller;

import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main")
public class ASongController {
    @Autowired
    ASongService aSongService;

    /**
     * 获取所有音乐分类
     * @return
     */
    @GetMapping("/getProClass")
    public Result getProClass(){

      return aSongService.getProClass();

    }

}
