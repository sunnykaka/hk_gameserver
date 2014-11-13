package com.origingame.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static byte[] encryp(byte[] content, byte[] password)
            throws AesException {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new AesException(e);
        } catch (NoSuchPaddingException e) {
            throw new AesException(e);
        } catch (InvalidKeyException e) {
            throw new AesException(e);
        } catch (IllegalBlockSizeException e) {
            throw new AesException(e);
        } catch (BadPaddingException e) {
            throw new AesException(e);
        }
    }

    public static byte[] decrypt(byte[] content, byte[] password)
            throws AesException {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (InvalidKeyException e) {
            throw new AesException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AesException(e);
        } catch (NoSuchPaddingException e) {
            throw new AesException(e);
        } catch (IllegalBlockSizeException e) {
            throw new AesException(e);
        } catch (BadPaddingException e) {
            throw new AesException(e);
        }
    }

    public static byte[] initPasswordKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom());
        SecretKey secretKey = kgen.generateKey();
        return secretKey.getEncoded();
    }

}