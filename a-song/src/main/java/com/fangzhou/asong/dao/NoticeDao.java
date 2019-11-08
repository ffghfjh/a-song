package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface NoticeDao extends JpaRepository<Notice,Long> {
    List<Notice> findNoticesByFormUserId(Long userId);
    List<Notice> findNoticesByFormUserIdAndCreateTime(Long userId, Date time);
    int removeByFormUserIdAndToUserId(Long fromId,Long toId);
    Notice findNoticeByFormUserIdAndToUserId(Long fromId,Long toId);
}
