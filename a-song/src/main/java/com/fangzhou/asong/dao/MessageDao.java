package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.SongMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageDao extends JpaRepository<SongMessage,Long> {
    List<SongMessage> findMessagesByUserId(Long userId);
    List<SongMessage> findMessagesByUserIdAndAndIsread(Long userId,boolean read);
}
