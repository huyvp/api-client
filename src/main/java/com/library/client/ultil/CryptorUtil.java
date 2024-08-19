package com.library.client.ultil;


import com.library.client.exception.CryptorException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author hc.yoon.c98test
 * 
 * This class is used to encrypt and decrypt a configuration data.
 * If you want to use other libraries, modify this class.
 *
 */
public class CryptorUtil {
    private static final String ENCRYPT_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPT_ALGORITHM = "AES";
    private static final String AES_KEY = "I love cafe babe";
    private static final String AES_IV = "0123456789abcdef"; //16byte

    private static final Cipher encryptor;
    private static final Cipher decryptor;

    static {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), ENCRYPT_ALGORITHM);
            IvParameterSpec ivParamSpec = new IvParameterSpec(AES_IV.getBytes());

            encryptor = Cipher.getInstance(ENCRYPT_TRANSFORMATION);
            decryptor = Cipher.getInstance(ENCRYPT_TRANSFORMATION);
            encryptor.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
            decryptor.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
        } catch (Exception e) {
            throw new CryptorException("Failed to initialize iPaaS Cryptor.", e);
        }
    }

    public static String encrypt(String originalData) {
        try {
            byte[] encryptedBytes = encryptor.doFinal(originalData.getBytes("UTF-8"));
            return new Base64Encoder().encode(encryptedBytes);
        } catch (Exception e) {
            throw new CryptorException("Failed to encrypt data.", e);
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            byte[] decodedBytes = new Base64Decoder().decodeBuffer(encryptedData);
            return new String(decryptor.doFinal(decodedBytes));
        } catch (Exception e) {
            throw new CryptorException("Failed to decrypt data.", e);
        }
    }
}
