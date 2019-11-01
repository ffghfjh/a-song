package com.fangzhou.asong.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;


public class WXBizDataCrypt {
    public static String illegalAesKey = "-41001";//非法密钥
    public static String illegalIv = "-41002";//非法初始向量
    public static String illegalBuffer = "-41003";//非法密文
    public static String decodeBase64Error = "-41004"; //解码错误
    public static String noData = "-41005"; //数据不正确
    private String appid;
    private String sessionKey;

    public WXBizDataCrypt(String appid, String sessionKey) {
        this.appid = appid;
        this.sessionKey = sessionKey;
    }

    public String decryptData(String encryptedData, String iv) {

        //非法密钥
        if (StringUtils.length(sessionKey) != 24) {
            return illegalAesKey;
        }
        byte[] aesKey = Base64.decodeBase64(sessionKey);
        //非法初始化向量
        if (StringUtils.length(iv) != 24) {
            return illegalIv;
        }
        byte[] aesIV = Base64.decodeBase64(iv);
        byte[] aesCipher = Base64.decodeBase64(encryptedData);

        try {
            byte[] resultByte = AESUtil.decrypt(aesCipher,aesKey,aesIV);
            if(null != resultByte && resultByte.length > 0){
                String userInfo = new String(resultByte,"UTF-8");
                JSONObject json = JSON.parseObject(userInfo);
                String id = json.getJSONObject("watermark").getString("apid");
                if(!StringUtils.equals(id,appid)){
                    return illegalBuffer;
                }
                return userInfo;
            }else {
                return noData;
            }
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

