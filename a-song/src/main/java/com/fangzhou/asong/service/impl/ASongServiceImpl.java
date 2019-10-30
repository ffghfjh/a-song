package com.fangzhou.asong.service.impl;

import com.fangzhou.asong.dao.ProductClassDao;
import com.fangzhou.asong.pojo.ProductionType;
import com.fangzhou.asong.service.ASongService;
import com.fangzhou.asong.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ASongServiceImpl implements ASongService {
    @Autowired
    ProductClassDao classDao;
    @Override
    public Result getProClass() {
        Iterable<ProductionType> types = classDao.findAll();
        return Result.success(types);
    }
}
