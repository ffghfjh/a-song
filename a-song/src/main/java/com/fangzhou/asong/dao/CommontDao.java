package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.ProCommont;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommontDao extends CrudRepository<ProCommont,Long> {
    List<ProCommont> findProCommontsByProId(Long proId);
}
