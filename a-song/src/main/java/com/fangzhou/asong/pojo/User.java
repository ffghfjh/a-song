package com.fangzhou.asong.pojo;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String header;//头像
    private String name;//名字
    private String birth;//生日
    private boolean isAuthor;//是否是作者
    private String province;//省份
    private String city;//市
    private String district;//区
    private String referrals;//推荐人
    private Date createTime;
    private Date updateTime;
    private String openid;
    private boolean isMan;
    private String phone;
    private int state;//状态

    public User(){

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isMan() {
        return isMan;
    }

    public void setMan(boolean man) {
        isMan = man;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public String getName() {
        return name;
    }

    public String getBirth() {
        return birth;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getReferrals() {
        return referrals;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public String getOpenid() {
        return openid;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public void setAuthor(boolean author) {
        isAuthor = author;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setReferrals(String referrals) {
        this.referrals = referrals;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", name='" + name + '\'' +
                ", birth='" + birth + '\'' +
                ", isAuthor=" + isAuthor +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", referrals='" + referrals + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", openid='" + openid + '\'' +
                '}';
    }
}