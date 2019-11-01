package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductDao extends CrudRepository<Product,Long> {
    Product findProductById(Long proId);
}
