package com.ppdai.open.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xuzhishen on 2016/3/16.
 */
public class OpenApiClient {

    /**
     * 获取授权信息URL
     */
    private final static String AUTHORIZE_URL = "https://ac.ppdai.com/oauth2/authorize";

    /**
     * 刷新Token信息URL
     */
    private final static String REFRESHTOKEN_URL = "https://ac.ppdai.com/oauth2/refreshtoken ";

    private final static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String appid;
    private static RsaCryptoHelper rsaCryptoHelper;

    public static void Init(String appid, RsaCryptoHelper.PKCSType pkcsTyps, String publicKey, String privateKey) throws Exception {
        OpenApiClient.appid = appid;
        rsaCryptoHelper = new RsaCryptoHelper(pkcsTyps, publicKey, privateKey);
    }

    /**
     * 向拍拍贷网关发�?�请�?
     *
     * @param url
     * @param propertyObjects
     * @return
     */
    public static Result send(String url, PropertyObject... propertyObjects) throws Exception {
        return send(url, 1, null, propertyObjects);
    }

    /**
     * 向拍拍贷网关发�?�请�?
     *
     * @param url
     * @param version
     * @param propertyObjects
     * @return
     */
    public static Result send(String url, double version, PropertyObject... propertyObjects) throws Exception {
        return send(url, version, null, propertyObjects);
    }


    /**
     * 向拍拍贷网关发�?�请�?
     *
     * @param url
     * @param accessToken
     * @param propertyObjects
     * @return
     */
    public static Result send(String url, String accessToken, PropertyObject... propertyObjects) throws Exception {
        return send(url, 1, accessToken, propertyObjects);
    }

    /**
     * 向拍拍贷网关发�?�请�?
     *
     * @param url
     * @param accessToken
     * @param propertyObjects
     * @return
     */
    public static Result send(String url, double version, String accessToken, PropertyObject... propertyObjects) throws Exception {
        if (appid == null || "".equals(appid)) throw new Exception("OpenApiClient未初始化");

        Result result = new Result();
        try {
            URL serviceUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) serviceUrl.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            /************** OpenApi�?有的接口都只提供Post方法 **************/
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            urlConnection.setRequestProperty("X-PPD-SIGNVERSION", "1");
            urlConnection.setRequestProperty("X-PPD-SERVICEVERSION", String.valueOf(version));

            /******************* 公共请求参数 ************************/
            urlConnection.setRequestProperty("X-PPD-APPID", appid);

            //获取UTC时间作为时间�?
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
            int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
            cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
            Long timestamp = (cal.getTime().getTime() - dateformat.parse("1970-01-01 00:00:00").getTime()) / 1000;
            urlConnection.setRequestProperty("X-PPD-TIMESTAMP", timestamp.toString());
            //对时间戳进行签名
            urlConnection.setRequestProperty("X-PPD-TIMESTAMP-SIGN", rsaCryptoHelper.sign(appid + timestamp).replaceAll("\\r", "").replaceAll("\\n", ""));

            String sign = rsaCryptoHelper.sign(ObjectDigitalSignHelper.getObjectHashString(propertyObjects)).replaceAll("\\r", "").replaceAll("\\n", "");
            urlConnection.setRequestProperty("X-PPD-SIGN", sign);
            if (accessToken != null && !"".equals(accessToken))
                urlConnection.setRequestProperty("X-PPD-ACCESSTOKEN", accessToken);
            /**************************************************************/

            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.writeBytes(propertyToJson(propertyObjects));
            dataOutputStream.flush();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String strResponse = bufferedReader.readLine();

            result.setSucess(true);
            result.setContext(strResponse);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        } finally {

        }
        return result;
    }

    /**
     * @param propertyObjects
     * @return
     */
    private static String propertyToJson(PropertyObject... propertyObjects) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        for (PropertyObject propertyObject : propertyObjects) {
            if (propertyObject.getValue() instanceof Integer) {
                node.put(propertyObject.getName(), (Integer) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof Long) {
                node.put(propertyObject.getName(), (Long) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof Float) {
                node.put(propertyObject.getName(), (Float) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof BigDecimal) {
                node.put(propertyObject.getName(), (BigDecimal) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof Double) {
                node.put(propertyObject.getName(), (Double) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof Boolean) {
                node.put(propertyObject.getName(), (Boolean) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof String) {
                node.put(propertyObject.getName(), (String) propertyObject.getValue());
            } else if (propertyObject.getValue() instanceof Date) {
                node.put(propertyObject.getName(), dateformat.format((Date) propertyObject.getValue()));
            } else {
                node.put(propertyObject.getName(), propertyObject.getValue().toString());
            }
        }

        return mapper.writeValueAsString(node);
    }

    /**
     * 获取授权
     *
     * @param code  授权�?
     * @return
     * @throws IOException
     */
    public static AuthInfo authorize(String code) throws Exception {
        if (appid == null || "".equals(appid)) throw new Exception("OpenApiClient未初始化");

        URL serviceUrl = new URL(AUTHORIZE_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) serviceUrl.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

        /******************** 获取授权参数 AppID code *********************/
        dataOutputStream.writeBytes(String.format("{\"AppID\":\"%s\",\"code\":\"%s\"}", appid, code));
        dataOutputStream.flush();
        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strResponse = bufferedReader.readLine();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(strResponse, AuthInfo.class);
    }

    /**
     * 刷新AccessToken
     *
     * @param openId       用户OpenID
     * @param refreshToken 刷新Token
     * @return
     * @throws IOException
     */
    public static AuthInfo refreshToken(String openId, String refreshToken) throws Exception {
        if (appid == null || "".equals(appid)) throw new Exception("OpenApiClient未初始化");

        URL serviceUrl = new URL(REFRESHTOKEN_URL);
        HttpURLConnection urlConnection = (HttpURLConnection) serviceUrl.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

        /****************** 刷新Token参数 AppID OpenID RefreshToken **********************/
        dataOutputStream.writeBytes(String.format("{\"AppID\":\"%s\",\"OpenID\":\"%s\",\"RefreshToken\":\"%s\"}", appid, openId, refreshToken));
        dataOutputStream.flush();
        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strResponse = bufferedReader.readLine();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(strResponse, AuthInfo.class);
    }
}
