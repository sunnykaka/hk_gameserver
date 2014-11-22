package com.origingame.util.crypto;

import com.origingame.exception.CryptoException;

/**
 * User: Liub
 * Date: 2014/11/14
 */
public interface CryptoStrategy {

    public byte[] encrypt(byte[] content, byte[] password) throws CryptoException;

    public byte[] decrypt(byte[] content, byte[] password) throws CryptoException;
}
