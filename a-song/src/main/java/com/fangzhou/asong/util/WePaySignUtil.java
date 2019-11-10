package com.fangzhou.asong.util;

import org.apache.tomcat.util.security.MD5Encoder;
import sun.security.provider.MD5;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class WePaySignUtil {

    /**
     * @Author : lilong
     * @Description :根据签名算法得出签名---参数按照参数名ASCII码从小到大排序（字典序）
     * @Date : 10:55 2018/6/5
     **/
    public static String createSign(SortedMap<String,Object> parameters, String key){
        StringBuffer sb = new StringBuffer();
        StringBuffer sbkey = new StringBuffer();
        Set es = parameters.entrySet();  //所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            //空值不传递，不参与签名组串
            if(null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
                sbkey.append(k + "=" + v + "&");
            }
        }
        //System.out.println("字符串:"+sb.toString());
        sbkey=sbkey.append("key="+key);
        System.out.println("签名字符串:"+sbkey.toString());
        //MD5加密,结果转换为大写字符

        String sign = MD5Util.MD5Encode(sbkey.toString()).toUpperCase();
        System.out.println("MD5加密值:"+sign);
        return sb.toString()+"sign="+sign;
    }


}
