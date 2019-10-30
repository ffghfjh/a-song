package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Author;
import org.springframework.data.repository.CrudRepository;

public interface AuthorDao extends CrudRepository<Author,Long> {

    Author findAuthorByUserIdAndState(Long uesrId,int state);

}
