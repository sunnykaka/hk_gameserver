package com.origingame.util.crypto;

import com.origingame.exception.CryptoException;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static byte[] encrypt(byte[] content, byte[] password)
            throws CryptoException {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static byte[] decrypt(byte[] content, byte[] password)
            throws CryptoException {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static byte[] initPasswordKey() throws CryptoException {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom());
            SecretKey secretKey = kgen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

}