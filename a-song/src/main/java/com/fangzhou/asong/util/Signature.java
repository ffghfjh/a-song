package com.fangzhou.asong.util;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.bouncycastle.jcajce.provider.digest.MD5;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Signature {
    public static String getSign(Object o,String key) throws IllegalAccessException{
        ArrayList<String> list = new ArrayList<>();
        Class cls = o.getClass();
        Field[] fields = cls.getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            if(f.get(o) !=null && f.get(o) != ""){
                String name = f.getName();
                XStreamAlias anno = f.getAnnotation(XStreamAlias.class);
                if(anno != null){
                    name = anno.value();
                    list.add(name+ "="+f.get(o)+"&");
                }
            }
        }
        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort,String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<size;i++){
            sb.append(arrayToSort[i]);
        }
        System.out.println("sb:"+sb.toString());
        String result = sb.toString();
        result += "key="+key;
        System.out.println("签名拼接字符串"+result);
        result = MD5Util.MD5Encode(result);
        return result;
    }

    public static String getSign(Map<String,Object> map,String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
            if(entry.getValue()!=""){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + key;
        //Util.log("Sign Before MD5:" + result);
        result = MD5Util.MD5Encode(result).toUpperCase();
        //Util.log("Sign Result:" + result);
        return result;
    }
}
