package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProductionType;
import org.springframework.data.repository.CrudRepository;

public interface ProductClassDao extends CrudRepository<ProductionType,Long> {
}
