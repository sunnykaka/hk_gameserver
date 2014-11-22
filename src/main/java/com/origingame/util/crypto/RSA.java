package com.origingame.util.crypto;

import com.origingame.exception.CryptoException;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class RSA {

    public static final String KEY_ALGORITHM = "RSA";

    public static final int KEY_SIZE = 1024;

    /**
     * 用公钥加密
     * @param data  加密数据
     * @param keyBytes   密钥
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data,byte[] keyBytes) throws CryptoException{
        //对公钥解密
//        byte[] keyBytes = Coder.decryptBASE64(key);
        //
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            //对数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 用公钥解密
     * @param data  加密数据
     * @param keyBytes   密钥
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data,byte[] keyBytes) throws CryptoException{
        try {
            //对私钥解密
    //        byte[] keyBytes = Coder.decryptBASE64(key);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            //对数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 用私钥加密
     * @param data  加密数据
     * @param keyBytes   密钥
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] keyBytes) throws CryptoException{
        try {
            //解密密钥
    //        byte[] keyBytes = Coder.decryptBASE64(key);
            //取私钥
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            //对数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 用私钥解密
     * @param keyBytes   密钥
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data,byte[] keyBytes) throws CryptoException{
        try {
            //对私钥解密
    //        byte[] keyBytes = Coder.decryptBASE64(key);

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            //对数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 初始化密钥
     *
     * @return
     * @throws Exception
     */
    public static Object[] initKey() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator
                    .getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(KEY_SIZE);

            KeyPair keyPair = keyPairGen.generateKeyPair();

            // 公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            // 私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            Map<String, Object> keyMap = new HashMap<String, Object>(2);

            return new Object[]{publicKey.getEncoded(), privateKey.getEncoded()};
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

//    public void generatePublicKey(final BigInteger modulus,
//			final BigInteger publicExponent) throws Exception {
//
//		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
//
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		keyFactory.generatePublic(keySpec).;
//
//	}

}