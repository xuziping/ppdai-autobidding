package test.java.com.ppdai.open.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.ppdai.open.core.AuthInfo;
import com.ppdai.open.core.ObjectDigitalSignHelper;
import com.ppdai.open.core.OpenApiClient;
import com.ppdai.open.core.PropertyObject;
import com.ppdai.open.core.Result;
import com.ppdai.open.core.RsaCryptoHelper;
import com.ppdai.open.core.ValueTypeEnum;

/**
 * Created by xuzhishen on 2016/3/16.
 */
public class BizTest {
	

	 private static Logger log = LogManager.getLogger(BizTest.class.getName());
	 
	 
    /************ 应用ＩＤ **************/
    private String  appid = "";

    /***************** 客户端私钥 **************/
    String privKey = "";

    /***************** 服务端公钥 ***************/
    String pubKey = "";


    /*********** 授权信息 ***************/
    private AuthInfo authInfo = null;

    /*********** 获取可投标列表 ***************/
    private  String Get_LoanList_URL = "http://gw.open.ppdai.com/invest/BidproductlistService/LoanList";


    /*********** 投标接口 ***************/
    private String Bid_URL = "http://gw.open.ppdai.com/invest/BidService/Bidding";

//    @Test
    public void AuthTest() throws Exception {

        /**
         * 跳转到AC的oauth2.0联合登录
         * https://ac.ppdai.com/oauth2/login?AppID=8cf65377538741c2ba8add2615a22299&ReturnUrl=http://mysite.com/auth/gettoken
         *
         */

        /**
         * 登录成功后 oauth2.0 跳转到http://mysite.com/auth/gettoken?code=XXXXXXXXXXXXXXXXXXXXXXXXXXX
         * 添加WebApi接口gettoken
         */
        gettoken("code");

        /**
         * 刷新Token
         * 用于AccessToken失效后刷新一个新的AccessToken，AccessToken有效期七天
         */
        refreshToken();
    }

    /**
     * 根据授权码获取授权信息
     * @param code
     * @throws IOException
     */
     void gettoken(String code) throws Exception {
         OpenApiClient.Init(appid, RsaCryptoHelper.PKCSType.PKCS8,pubKey,privKey);
        authInfo =  OpenApiClient.authorize(code);
    }

    /**
     * 刷新令牌
     * @throws IOException
     */
     void refreshToken() throws Exception {
         OpenApiClient.Init(appid, RsaCryptoHelper.PKCSType.PKCS8,pubKey,privKey);
         authInfo = OpenApiClient.refreshToken(authInfo.getOpenID(),authInfo.getRefreshToken());
    }

//@Test
    public void getSignString() throws ParseException {

        final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String str2 = ObjectDigitalSignHelper.getObjectHashString(
                new PropertyObject("Age", 18, ValueTypeEnum.Int32),
                new PropertyObject("Amount", BigDecimal.valueOf(154.254), ValueTypeEnum.Decimal),
                new PropertyObject("ByteProperty", Byte.parseByte("5"), ValueTypeEnum.Byte),
                new PropertyObject("CharProperty", 'C', ValueTypeEnum.Char),
                new PropertyObject("CreateDate", dateformat.parse("2016-03-14 19:15:22"), ValueTypeEnum.DateTime),
                new PropertyObject("Data", new ArrayList<String>(), ValueTypeEnum.Other),
                new PropertyObject("DoubleProperty", 3.14159265358979, ValueTypeEnum.Double),
                new PropertyObject("GuidProperty", UUID.fromString("f1f55e34-1a12-41c1-bd51-00341f3eacb8"), ValueTypeEnum.Guid),
                new PropertyObject("ID", 55, ValueTypeEnum.Int16),
                new PropertyObject("Int64Property", 12365478958745487L, ValueTypeEnum.Int64),
                new PropertyObject("IsVIP", false, ValueTypeEnum.Boolean),
                new PropertyObject("Message", "hello world", ValueTypeEnum.String),
                new PropertyObject("SByetProperty", Byte.parseByte("3"), ValueTypeEnum.SByte),
                new PropertyObject("SingleProperty", 25.5687F, ValueTypeEnum.Single),
                new PropertyObject("UInt16Property", 15, ValueTypeEnum.UInt16),
                new PropertyObject("UInt32Property", 256, ValueTypeEnum.UInt32),
                new PropertyObject("UInt64Property", 198745874512L, ValueTypeEnum.UInt64)
        );

        log.info(str2);

    }

//    @Test
    public void InterfaceTest()throws Exception{
        String gwurl = "http://gw.open.ppdai.com";
        String token = "74c84db8-db26-4d10-8f48-6658ea03ed89";

        OpenApiClient.Init(appid, RsaCryptoHelper.PKCSType.PKCS8,pubKey,privKey);

        Result result;
        log.info("测试 AuthService.SendSMSAuthCode");
        result = OpenApiClient.send(gwurl + "/auth/authservice/sendsmsauthcode"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("DeviceFP", "asdfasdf4asdf546asf", ValueTypeEnum.String));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 AuthService.SMSAuthCodeLogin");
        result = OpenApiClient.send(gwurl + "/open/oauthservice/smsauthcodelogin"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("DeviceFP", "asdfasdf4asdf546asf", ValueTypeEnum.String)
                , new PropertyObject("SMSAuthCode", "111111", ValueTypeEnum.String));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 RegisterService.Register");
        result = OpenApiClient.send(gwurl + "/auth/registerservice/register"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("Email", "xxxxxx@ppdai.com", ValueTypeEnum.String)
                , new PropertyObject("Role", 12, ValueTypeEnum.Int32));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 RegisterService.Register 2");
        result = OpenApiClient.send(gwurl + "/open/registerservice/register"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("Email", "xxxxxx@ppdai.com", ValueTypeEnum.String)
                , new PropertyObject("Role", 12, ValueTypeEnum.Int32));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 RegisterService.SendSMSRegisterCode");
        result = OpenApiClient.send(gwurl + "/auth/registerservice/sendsmsregistercode"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("DeviceFP", "asdfasdf4asdf546asf", ValueTypeEnum.String));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 RegisterService.AccountExist");
        result = OpenApiClient.send(gwurl + "/auth/registerservice/AccountExist"
                , new PropertyObject("AccountName", "15200000001", ValueTypeEnum.String));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 RegisterService.SMSCodeRegister");
        result = OpenApiClient.send(gwurl + "/open/registerservice/smscoderegister"
                , new PropertyObject("Mobile", "15200000001", ValueTypeEnum.String)
                , new PropertyObject("DeviceFP", "asdfasdf4asdf546asf", ValueTypeEnum.String)
                , new PropertyObject("Code", "111111", ValueTypeEnum.String));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 AutoLogin.AutoLogin");
        result = OpenApiClient.send(gwurl + "/auth/LoginService/AutoLogin", token
                , new PropertyObject("Timestamp", new Date(), ValueTypeEnum.DateTime));
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

        log.info("测试 AutoLogin.QueryUserInfo");
        result = OpenApiClient.send(gwurl + "/auth/LoginService/QueryUserInfo", token);
        log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
        log.info("");
        log.info("-----------------------------------------------");
        log.info("");

    }

//    @Test
    public void pkcs1Test()throws Exception{
        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8iMpEG3mnFlMfufO95DfAfor80RL3I/IzF828aoDDw/Xy86jPiihJyGyG2ZmbqsAw+8nj8eGc+U9LmKASgQhS9e0R/MmYDa9R/O2f4tQZUQr3nE3uUTES0tqCLoE3TVSd59lnVExeDL5IW+F/Yc9mz1v+xSDFcSKyfHEo0FDnnwIDAQAB";
        String privKey = "MIICWwIBAAKBgQC8iMpEG3mnFlMfufO95DfAfor80RL3I/IzF828aoDDw/Xy86jPiihJyGyG2ZmbqsAw+8nj8eGc+U9LmKASgQhS9e0R/MmYDa9R/O2f4tQZUQr3nE3uUTES0tqCLoE3TVSd59lnVExeDL5IW+F/Yc9mz1v+xSDFcSKyfHEo0FDnnwIDAQABAoGAJ5wxqrd/CpzFIBBIZmfxUq8DcnRWoLfbpeJlZiWWIgskvEN2/wuOxVmne3lyLWNld6Ue2JY0CW/TuhU55ElZvv91NiTreBqr5WfZ8EYI+/lwEUKC4GzogVwrmpL1PpSaNJymvTujiShmP/+hia2mav9fhMOYm8MaMRwPELwASiECQQD0nW8xWF9IRT90v89y+P/htW+g3E4HZVAYPXyhfAnFJsGC06XAXwO0hDS8Sao7Nktj2sNSacNFjZvndGrQPOePAkEAxU8o7+QHqm/HYsO0XN49xn6zWQRvAOonhl5/+NKm7NfGEVTGwhP5KbNsJPv3TTtCPrS2V6MlIScg1yLXkFF28QJAGoEYdDNMF6uRJZhG5QE/0Hf1QWu9dKWwmP/IikLDWD5Lx14hXoetAhk1EZW1wTav0oD4muxkwRuH4ftGO4vt1wJAKkjdsBOBZRBRfaQNWj2ypYBvtSsTEvIbiFtmN5AFgAp6AyrU8bDQHBS8n2x0QlPpzYBy93MaOPGmwxRPeDlNMQJAKubPrAE9Qe++95xvvfpZgj6wOZoKGa4Yj3dd1PYcO2fU9eVSW1W6IrvJc36NIGz4Egyw2EiqFBBIJL92ZhjQ2Q==";
        String txt = "abc";

        RsaCryptoHelper rsaCryptoHelper = new RsaCryptoHelper(RsaCryptoHelper.PKCSType.PKCS1,pubKey,privKey);


        String txt2 = rsaCryptoHelper.encryptByPublicKey(txt);
        String txt3 = rsaCryptoHelper.decryptByPrivateKey(txt2);

        String sign = rsaCryptoHelper.sign(txt);
        boolean isSign = rsaCryptoHelper.verify(txt,sign);
    }
//@Test
    public void pkcs8Test()throws Exception{

        String pubKey =
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDORZqYdrpLFHv+TZI8xdC8ikMYoFzvaCWIfzvz04WxQ/lJY7cQBTiWP+zmU2PAb+c7sIjp7e9kzadgwkhgjDhGj9707+NND/q1zrWRblctOaEF2l1Fh8sqsv/id833Loq4JZYosfmBNKTKe8lvpHfTB/WZUrYMZb4yVoNg/9w1KwIDAQAB";
        String privKey = 
                "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM5Fmph2uksUe/5NkjzF0LyKQxigXO9oJYh/O/PThbFD+UljtxAFOJY/7OZTY8Bv5zuwiOnt72TNp2DCSGCMOEaP3vTv400P+rXOtZFuVy05oQXaXUWHyyqy/+J3zfcuirglliix+YE0pMp7yW+kd9MH9ZlStgxlvjJWg2D/3DUrAgMBAAECgYAovXabRlflHFahE5Eej7N6iZRW+NViM5+2JeshbVWqfVJvPFKbx2w1wMp5c17wUynIkEV6bpQpxLSaV8UTzJ4QdsTOkYMYVz3WhWoWZao+uk2DD0zGtx342zinWQQDqjUt3SzAnoMo9DwznUVr8dZnsbDWyBSTC6JtMKpF3s7iAQJBAPTq3HbwSo3MCBT9DSjP5sYquxZSnxyymLX2Tyk87bxR/DQ7FtxUk4HCS5MP1hzeq5XpOlneT0w1F//FFB84pdECQQDXmxIEyGl2qxV0F4gS4qO1/SXOK8HfIwNomN84MOefTDGcqAIbL7Kx1V3P3SzY4HskWgw0bODadK0bGuNSx547AkAlHO3ZjCIQCKn03D/BPnfe8Zy2DkEULTAc6r0mJ5hy4A2SsJ2PN7W+hP3ExDKS318q8VOpSJnFl4oSdP/Ol1vxAkBBJgdk0JYlmH6sDKw+YKNtS2gQC2LSpQbTpVXV6dkjZmebWZ0BUAFkAQAO3ls90V8EVf1YHgo3mIfyJ8bG7bCfAkEA7YZtsJq+MZZ2KwvikQdJGrc8r5Kpqrzl8T+q5TUIa1eiu/mrwWgVDi4aUHMa+aPyQ6F2TeYvr0WVYbvCgGjT0Q==";
             
        String txt = "abc";

        RsaCryptoHelper rsaCryptoHelper = new RsaCryptoHelper(RsaCryptoHelper.PKCSType.PKCS8,pubKey,privKey);


        String txt2 = rsaCryptoHelper.encryptByPublicKey(txt);
        String txt3 = rsaCryptoHelper.decryptByPrivateKey(txt2);

        String sign = rsaCryptoHelper.sign(txt);
        boolean isSign = rsaCryptoHelper.verify(txt,sign);
    }

@Test
public void pkcs8AccountExitss() throws Exception{
    String pubKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDORZqYdrpLFHv+TZI8xdC8ikMYoFzvaCWIfzvz04WxQ/lJY7cQBTiWP+zmU2PAb+c7sIjp7e9kzadgwkhgjDhGj9707+NND/q1zrWRblctOaEF2l1Fh8sqsv/id833Loq4JZYosfmBNKTKe8lvpHfTB/WZUrYMZb4yVoNg/9w1KwIDAQAB";
    String privKey = 
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM5Fmph2uksUe/5NkjzF0LyKQxigXO9oJYh/O/PThbFD+UljtxAFOJY/7OZTY8Bv5zuwiOnt72TNp2DCSGCMOEaP3vTv400P+rXOtZFuVy05oQXaXUWHyyqy/+J3zfcuirglliix+YE0pMp7yW+kd9MH9ZlStgxlvjJWg2D/3DUrAgMBAAECgYAovXabRlflHFahE5Eej7N6iZRW+NViM5+2JeshbVWqfVJvPFKbx2w1wMp5c17wUynIkEV6bpQpxLSaV8UTzJ4QdsTOkYMYVz3WhWoWZao+uk2DD0zGtx342zinWQQDqjUt3SzAnoMo9DwznUVr8dZnsbDWyBSTC6JtMKpF3s7iAQJBAPTq3HbwSo3MCBT9DSjP5sYquxZSnxyymLX2Tyk87bxR/DQ7FtxUk4HCS5MP1hzeq5XpOlneT0w1F//FFB84pdECQQDXmxIEyGl2qxV0F4gS4qO1/SXOK8HfIwNomN84MOefTDGcqAIbL7Kx1V3P3SzY4HskWgw0bODadK0bGuNSx547AkAlHO3ZjCIQCKn03D/BPnfe8Zy2DkEULTAc6r0mJ5hy4A2SsJ2PN7W+hP3ExDKS318q8VOpSJnFl4oSdP/Ol1vxAkBBJgdk0JYlmH6sDKw+YKNtS2gQC2LSpQbTpVXV6dkjZmebWZ0BUAFkAQAO3ls90V8EVf1YHgo3mIfyJ8bG7bCfAkEA7YZtsJq+MZZ2KwvikQdJGrc8r5Kpqrzl8T+q5TUIa1eiu/mrwWgVDi4aUHMa+aPyQ6F2TeYvr0WVYbvCgGjT0Q==";
           
    String url = "http://gw.open.ppdai.com/invest/LLoanInfoService/BatchListingInfos";

    OpenApiClient.Init("45376b4859794aa8996957d69f96ce42", RsaCryptoHelper.PKCSType.PKCS8,pubKey,privKey);
    log.info("测试 RegisterService.AccountExist");
    Result result = OpenApiClient.send(url    		
            , new PropertyObject("AccountName", "pdu2608055825", ValueTypeEnum.String));
    log.info(String.format("返回结果:%s", result.isSucess() ? result.getContext() : result.getErrorMessage()));
    log.info("");
    log.info("-----------------------------------------------");
    log.info("");
}
}
