package com.fangzhou.asong.util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.parser.Entity;


public class HttpUtil {
    static CloseableHttpClient client;
    static CloseableHttpResponse response;



    static HttpPost post;//post请求
    static HttpGet get;//get请求
    static ObjectMapper objectmapper;



    /**
     * post请求
     * @param URL 请求地址
     * @param map 请求体参数map集合
     * @return json对象
     */
    public static JSONObject postToJSONObject(String URL, Map<String,Object> map){
        post = new HttpPost(URL);
        objectmapper = new ObjectMapper();
        HttpEntity entity;
        try {
            entity = new StringEntity(objectmapper.writeValueAsString(map));
            post.setEntity(entity);
            client = HttpClients.createDefault();
            response = client.execute(post);

            HttpEntity repentity = response.getEntity();
            String resresult = EntityUtils.toString(repentity);

            JSONObject object = JSON.parseObject(resresult);
            return object;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject postToJSONObject(String URL, SortedMap<Object,Object> parameters){
        post = new HttpPost(URL);
        objectmapper = new ObjectMapper();
        HttpEntity entity;
        try {
            entity = new StringEntity(objectmapper.writeValueAsString(parameters));
            post.setEntity(entity);
            client = HttpClients.createDefault();
            response = client.execute(post);
            HttpEntity repentity = response.getEntity();
            String resresult = EntityUtils.toString(repentity);
            JSONObject object = JSON.parseObject(resresult);
            return object;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * get请求
     * @param URL 请求地址
     * @return json对象
     */
    public static JSONObject getToJSONObject(String URL){
        get = new HttpGet(URL);
        try {
            client = HttpClients.createDefault();
            response = client.execute(get);
            HttpEntity repentity = response.getEntity();
            String resresult = EntityUtils.toString(repentity);
            JSONObject json = JSON.parseObject(resresult);
            return json;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
