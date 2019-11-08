package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.AuthorOwnProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnProducterDao extends JpaRepository<AuthorOwnProduct,Long> {
    AuthorOwnProduct findAuthorOwnProductByAuthorId(Long authorId);
    //根据贡献数升序排序
    List<AuthorOwnProduct> findAuthorOwnProductsByOrderByNumAsc();
}
