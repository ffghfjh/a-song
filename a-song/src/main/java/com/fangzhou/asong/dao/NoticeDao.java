package com.fangzhou.asong.dao;

import com.fangzhou.asong.pojo.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface NoticeDao extends JpaRepository<Notice,Long> {
    String sql = "select * from notice where form_user_id = ?1 and year(update_time) = ?2 and month(update_time) = ?3 and day(update_time) = ?4";
    List<Notice> findNoticesByFormUserId(Long userId);
    @Query(value = sql,nativeQuery = true)
    List<Notice> findNoticeByDate(Long userId, String year,String month,String day);
    int removeByFormUserIdAndToUserId(Long fromId,Long toId);
    Notice findNoticeByFormUserIdAndToUserId(Long fromId,Long toId);
}
