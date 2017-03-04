package com.ppdai.open.core;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by xuzhishen on 2016/3/15.
 */
public class RsaCryptoHelper {

    private static final String KEY_ALGORTHM="RSA";//
    private static final String SIGNATURE_ALGORITHM="SHA1withRSA";

    private static KeyFactory keyFactory;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public enum PKCSType{
        PKCS1,
        PKCS8
    }

    /**
     * 创建密钥对象
     * @param pkcsTyps  私钥填充方式,支持PKCS1和PKCS8
     * @param publicKey 公钥
     * @param privateKey    私钥
     * @throws Exception
     */
    public RsaCryptoHelper(PKCSType pkcsTyps, String publicKey, String privateKey) throws Exception {
        publicKey = tremEnter(publicKey);
        privateKey = tremEnter(privateKey);

        //PEM转ASN1
        if (publicKey.startsWith("----")) {
            int fidx = publicKey.indexOf("\n");
            int lidx = publicKey.lastIndexOf("\n");
            publicKey = publicKey.substring(fidx + 1, lidx).replaceAll("\n", "");
        }
        if (privateKey.startsWith("----")) {
            int fidx = privateKey.indexOf("\n");
            int lidx = privateKey.lastIndexOf("\n");
            privateKey = privateKey.substring(fidx + 1, lidx).replaceAll("\n", "");
        }

        //初始化公私钥
        this.keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        //初始化公�?
        byte[] pubKeyBytes = decryptBASE64(publicKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        this.publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        //初始化私�?
        byte[] keyBytes = decryptBASE64(privateKey);
        if (pkcsTyps == PKCSType.PKCS1) {
            RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(keyBytes));
            RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
            this.privateKey = keyFactory.generatePrivate(rsaPrivKeySpec);
        } else if (pkcsTyps == PKCSType.PKCS8) {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            this.privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        }
    }

    private static String tremEnter(String str){
        int fidx = 0,lidx = str.length() -1;
        while (fidx < str.length()){
            if (str.charAt(fidx) != '\n') break;
            fidx++;
        }

        while (lidx >= 0){
            if (str.charAt(lidx) != '\n') break;
            lidx --;
        }

        if (fidx > lidx) return "";

        return str.substring(fidx,lidx+1);
    }

    /**
     * BASE64解密
     * @param key
     * @return
     * @throws Exception
     */
    private static byte[] decryptBASE64(String key) throws Exception{
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /**
     * BASE64加密
     * @param key
     * @return
     * @throws Exception
     */
    private static String encryptBASE64(byte[] key)throws Exception{
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    /**
     * 用私钥解�? * @param data    加密数据
     * @return
     * @throws Exception
     */
    public byte[] decryptByPrivateKey(byte[] data)throws Exception{
        //对数据解�?
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 用私钥解�?
     * @param data   加密数据
     * @return
     * @throws Exception
     */
    public String decryptByPrivateKey(String data)throws Exception {
        return new String(decryptByPrivateKey(decryptBASE64(data)));
    }

    /**
     * 用公钥加�?
     * @param data   加密数据
     * @return
     * @throws Exception
     */
    public byte[] encryptByPublicKey(byte[] data)throws Exception{
        //对数据解�?
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 用公钥加�?
     * @param data   加密数据
     * @return
     * @throws Exception
     */
    public String encryptByPublicKey(String data)throws Exception {
        return encryptBASE64(encryptByPublicKey(data.getBytes()));
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data   //加密数据
     * @return
     * @throws Exception
     */
    public String sign(byte[] data)throws Exception{
        //用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);

        return encryptBASE64(signature.sign());
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data   //加密数据
     * @return
     * @throws Exception
     */
    public String sign(String data)throws Exception{
        return sign(data.getBytes());
    }

    /**
     * 校验数字签名
     * @param data   加密数据
     * @param sign   数字签名
     * @return
     * @throws Exception
     */
    public boolean verify(byte[] data, String sign)throws Exception{
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        //验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * 校验数字签名
     * @param data   加密数据
     * @param sign   数字签名
     * @return
     * @throws Exception
     */
    public boolean verify(String data,String sign)throws Exception{
        return verify(data.getBytes(),sign);
    }
}
