package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Product;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
@CacheConfig(cacheNames = "product")
public interface ProductDao extends JpaRepository<Product,Long> {
    Product findProductById(Long proId);
    List<Product> findAllByOrderByCreateTimeAsc();
    List<Product> findProductsByAuthorId(Long authorId);
    //按点赞量升序排序
    List<Product> findAllByOrderByGoodNumAsc();

    //获取销量热门的作品
    List<Product> findAllByOrderByPayNumAsc();

    List<Product> findProductsByClassIdOrderByGoodNumAsc(int classId);

    List<Product> findProductsByClassIdAndRecommendOrderByPayNum(int classId,boolean recommend);

    List<Product> findProductsByClassIdOrderByPayNum(int classId);

    List<Product> findProductsByRecommendOrderByPayNum(Boolean recommend);



}
