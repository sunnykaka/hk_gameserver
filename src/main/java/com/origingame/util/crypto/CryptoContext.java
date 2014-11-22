package com.origingame.util.crypto;

import com.origingame.exception.CryptoException;

/**
 * User: Liub
 * Date: 2014/11/14
 */
public class CryptoContext{

    private CryptoStrategy strategy;

    private byte[] password;

    private CryptoContext() {}

    public static CryptoContext create(CryptoStrategy strategy, byte[] password) {
        CryptoContext cryptoContext = new CryptoContext();
        cryptoContext.strategy = strategy;
        cryptoContext.password = password;
        return cryptoContext;
    }

    public static CryptoContext createAESCrypto(byte[] password) {
        return create(new AESCrypto(), password);
    }

    public static CryptoContext createRSAServerCrypto(byte[] password) {
        return create(new RSAServerCrypto(), password);
    }

    public static CryptoContext createRSAClientCrypto(byte[] password) {
        return create(new RSAClientCrypto(), password);
    }


    public byte[] encrypt(byte[] content) throws CryptoException {
        return strategy.encrypt(content, password);
    }

    public byte[] decrypt(byte[] content) throws CryptoException {
        return strategy.decrypt(content, password);
    }


    public static class AESCrypto implements CryptoStrategy {

        @Override
        public byte[] encrypt(byte[] content, byte[] password) throws CryptoException {
            return AES.encrypt(content, password);
        }

        @Override
        public byte[] decrypt(byte[] content, byte[] password) throws CryptoException {
            return AES.decrypt(content, password);
        }
    }

    public static class RSAServerCrypto implements CryptoStrategy {

        @Override
        public byte[] encrypt(byte[] content, byte[] password) throws CryptoException {
            return RSA.encryptByPublicKey(content, password);
        }

        @Override
        public byte[] decrypt(byte[] content, byte[] password) throws CryptoException {
            return RSA.decryptByPublicKey(content, password);
        }
    }


    public static class RSAClientCrypto implements CryptoStrategy {

        @Override
        public byte[] encrypt(byte[] content, byte[] password) throws CryptoException {
            return RSA.encryptByPrivateKey(content, password);
        }

        @Override
        public byte[] decrypt(byte[] content, byte[] password) throws CryptoException {
            return RSA.decryptByPrivateKey(content, password);
        }
    }
}
