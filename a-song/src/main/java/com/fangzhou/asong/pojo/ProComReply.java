package com.fangzhou.asong.pojo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * 回复
 */
@Entity
public class ProComReply {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private String context;
    private Long replyForId;//回复Id
    private int replyForType;//回复类型 1评论 2回复
    private Date CreateTime;
    private Date UpdateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Long getReplyForId() {
        return replyForId;
    }

    public void setReplyForId(Long replyForId) {
        this.replyForId = replyForId;
    }

    public int getReplyForType() {
        return replyForType;
    }

    public void setReplyForType(int replyForType) {
        this.replyForType = replyForType;
    }

    public Date getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Date createTime) {
        CreateTime = createTime;
    }

    public Date getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(Date updateTime) {
        UpdateTime = updateTime;
    }
}
