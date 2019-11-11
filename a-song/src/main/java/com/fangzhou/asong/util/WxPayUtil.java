package com.fangzhou.asong.util;

import com.github.wxpay.sdk.WXPayConstants;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.alibaba.fastjson.util.IOUtils.UTF8;

public class WxPayUtil {

    public static String sendPost(Map<String,String> map,String url,int connectTimeoutMs, int readTimeoutMs) throws Exception {
            String xml = mapToXml(map);
            URL httpUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(connectTimeoutMs);
            httpURLConnection.setReadTimeout(readTimeoutMs);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(xml.getBytes(UTF8));
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            String resp = stringBuffer.toString();
            if (stringBuffer != null) {
                try {
                    bufferedReader.close();
                } catch (IOException var18) {
                    var18.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException var17) {
                    var17.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException var16) {
                    var16.printStackTrace();
                }
            }
            return resp;
    }


    public static String mapToXml(Map<String, String> data) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("xml");
        document.appendChild(root);
        Iterator var5 = data.keySet().iterator();

        while(var5.hasNext()) {
            String key = (String)var5.next();
            String value = (String)data.get(key);
            if (value == null) {
                value = "";
            }

            value = value.trim();
            Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString();

        try {
            writer.close();
        } catch (Exception var12) {
        }

        return output;
    }



    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        Map<String, String> data = new HashMap();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        Document doc = documentBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getDocumentElement().getChildNodes();

        for(int idx = 0; idx < nodeList.getLength(); ++idx) {
            Node node = nodeList.item(idx);
            if (node.getNodeType() == 1) {
                Element element = (Element)node;
                data.put(element.getNodeName(), element.getTextContent());
            }
        }

        try {
            stream.close();
        } catch (Exception var10) {
        }

        return data;
    }

    public static String getSign(Map<String, String> data,String key) throws Exception {

        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.FIELD_SIGN)) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
        }
        sb.append("key=").append(key);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] array = new byte[0];
        try {
            array = md.digest(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb2 = new StringBuilder();
        for (byte item : array) {
            sb2.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb2.toString().toUpperCase();
    }
}
