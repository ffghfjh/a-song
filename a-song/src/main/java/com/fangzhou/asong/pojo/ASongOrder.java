package com.fangzhou.asong.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class ASongOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;//下单者
    private String orderNum;//订单号
    private Date createTime;
    private Date updateTime;
    private String productId;
    private int state; //订单状态
    private float money;//订单金额

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

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

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
    @Temporal(value = TemporalType.DATE)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ASongOrder{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderNum='" + orderNum + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", productId='" + productId + '\'' +
                ", state=" + state +
                ", money=" + money +
                '}';
    }
}
