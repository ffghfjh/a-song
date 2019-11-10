package com.fangzhou.asong.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};


    /**
     * 转换字节数组为16进制字符串
     * @param b
     * @return
     */
    public static String byteArrayToHexString(byte[] b){
        StringBuilder resultSb = new StringBuilder();
        for(byte aB : b){
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    /**
     * 将byte转16进制
     * @param b
     * @return
     */
    private static String byteToHexString(byte b){
        int n = b;
        if(n<0){
            n = 256 + n;
        }
        int d1 = n/16;
        int d2 = n%16;
        return hexDigits[d1]+hexDigits[d2];
    }

    /**
     * MD5编码
     * @param origin
     * @return
     */
    public static String MD5Encode(String origin){
        String resultString = null;
        try{
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return resultString;
    }



}
