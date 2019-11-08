package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Author;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@CacheConfig(cacheNames = "product")
public interface AuthorDao extends CrudRepository<Author,Long> {

    Author findAuthorByUserIdAndState(Long uesrId,int state);

    @Cacheable(key = "#p0")
    Author findAuthorById(Long id);

    List<Author> findAuthorsByState(int state);



}
