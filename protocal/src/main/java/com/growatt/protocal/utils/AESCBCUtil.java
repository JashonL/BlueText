package com.growatt.protocal.utils;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-CBC数据加解密工具类
 */
public class AESCBCUtil {




/*    private static String SECRET_KEY = "growatt_key16aes";//
    private static String iv = "growatt_Ivs16aes";//偏移量字符串16位 当模式是CBC的时候必须设置偏移量*/
    private static String Algorithm = "AES";



    private static final String SECRET_KEY = "growatt_aes16key";//
    private static final String iv = "growatt_aes16Ivs";//偏移量字符串16位 当模式是CBC的时候必须设置偏移量



    public static byte[] AESEncryption(byte[] data) throws Exception {
        // 直接使用固定IV，不要随机生成
        IvParameterSpec ivSpec = new IvParameterSpec(
                iv.getBytes(StandardCharsets.UTF_8)
        );

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                Algorithm
        );

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(data);  // 只返回密文
    }

    public static byte[] AESDecryption(byte[] encryptedData) throws Exception {
        // 直接使用固定IV
        IvParameterSpec ivSpec = new IvParameterSpec(
                iv.getBytes(StandardCharsets.UTF_8)
        );

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey secretKey = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                Algorithm
        );

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(encryptedData);  // 直接解密密文
    }

}
