package com.origingame.server;


import com.origingame.util.crypto.AES;
import com.origingame.util.crypto.RSA;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class EncryptionTest {

    public static final String KEY_ALGORTHM="RSA";//

    @Before
    public void init() {
        System.out.println("before");
    }



    @Test
    public void testRSA() throws Exception{

        String before = "你叫什么名字啊";
        String after = null;

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORTHM);
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //私钥
        RSAPrivateKey privateKey =  (RSAPrivateKey) keyPair.getPrivate();


        byte[] publicKeyEncode = publicKey.getEncoded();
        byte[] bytesEncryptedByPrivateKey = RSA.encryptByPrivateKey(before.getBytes(Charset.forName("UTF-8")), privateKey.getEncoded());
        byte[] bytesDecryptedByPublicKey = RSA.decryptByPublicKey(bytesEncryptedByPrivateKey, publicKeyEncode);
        after = new String(bytesDecryptedByPublicKey, Charset.forName("UTF-8"));

        assertThat(after, is(before));

    }

    @Test
    public void testAES() throws Exception{

        String before = "你叫什么名字啊";
        String after = null;

        byte[] passwordKey = AES.initPasswordKey();

        after = new String(AES.decrypt(AES.encrypt(before.getBytes(Charset.forName("UTF-8")), passwordKey), passwordKey), Charset.forName("UTF-8"));

        assertThat(after, is(before));


    }

}
