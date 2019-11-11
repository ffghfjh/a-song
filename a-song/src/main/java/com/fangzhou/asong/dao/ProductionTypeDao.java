package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProductionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionTypeDao extends JpaRepository<ProductionType,Long> {

    ProductionType findProductionTypeByName(String name);
}
