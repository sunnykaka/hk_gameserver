package com.origingame.util;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA {

//	private PublicKey publicKey;
//
//	public RSA() {
//		super();
//	}
//
//	public void generatePublicKey(final BigInteger modulus,
//			final BigInteger publicExponent) throws Exception {
//
//		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
//
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		publicKey = keyFactory.generatePublic(keySpec);
//
//	}
//
//	public void generatePublicKey(final byte[] modulus,
//			final byte[] publicExponent) throws Exception {
//
//		BigInteger m = new BigInteger(modulus);
//		BigInteger p = new BigInteger(publicExponent);
//
//		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, p);
//
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		publicKey = keyFactory.generatePublic(keySpec);
//
//	}
//
//	public byte[] encrypt(byte[] plainText)
//			throws InvalidKeyException, NoSuchAlgorithmException,
//			NoSuchPaddingException, IllegalBlockSizeException,
//			BadPaddingException {
//
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		byte[] enBytes = cipher.doFinal(plainText);
//
//		return enBytes;
//	}
//
//	public byte[] encrypt(String plainText)
//			throws InvalidKeyException, NoSuchAlgorithmException,
//			NoSuchPaddingException, IllegalBlockSizeException,
//			BadPaddingException {
//
//		byte[] bytes = plainText.getBytes();
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		byte[] enBytes = cipher.doFinal(bytes);
//
//		return enBytes;
//	}
//
//
//	public PublicKey getPublicKey() {
//		return publicKey;
//	}
//
//	public void setPublicKey(PublicKey publicKey) {
//		this.publicKey = publicKey;
//	}

    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 用公钥加密
     * @param data  加密数据
     * @param key   密钥
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data,byte[] keyBytes)throws Exception{
        //对公钥解密
//        byte[] keyBytes = Coder.decryptBASE64(key);
        //取公钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        //对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 用公钥解密
     * @param data  加密数据
     * @param key   密钥
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data,byte[] keyBytes)throws Exception{
        //对私钥解密
//        byte[] keyBytes = Coder.decryptBASE64(key);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        //对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 用私钥加密
     * @param data  加密数据
     * @param key   密钥
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] keyBytes)throws Exception{
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
    }

    /**
     * 用私钥解密<span style="color:#000000;"></span> * @param data  加密数据
     * @param key   密钥
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data,byte[] keyBytes)throws Exception{
        //对私钥解密
//        byte[] keyBytes = Coder.decryptBASE64(key);

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        //对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
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