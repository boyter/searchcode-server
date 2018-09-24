/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.searchcode.app.config.Values;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESEncryptor {
    private byte[] key;

    private final String ALGORITHM = "AES";

    public AESEncryptor() {
        String encryptionKey = Properties.getProperties().getProperty(Values.ENCRYPTION_KEY, Values.DEFAULT_ENCRYPTION_KEY);
        this.setKey(encryptionKey);
    }

    public AESEncryptor(String key) {
        this.setKey(key);
    }

    public String encryptToBase64String(String plainText) throws Exception {
        byte[] cipherText = this.encrypt(plainText.getBytes());
        return new String(Base64.getEncoder().encode(cipherText));
    }

    public String decryptFromBase64String(String cipherText) throws Exception {
        byte[] plainText = this.decrypt(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    public byte[] encrypt(byte[] plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(this.key, this.ALGORITHM);
        Cipher cipher = Cipher.getInstance(this.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(plainText);
    }

    public byte[] decrypt(byte[] cipherText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(this.key, this.ALGORITHM);
        Cipher cipher = Cipher.getInstance(this.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(cipherText);
    }

    private void setKey(String key) {
        this.key = DigestUtils.sha1Hex(key).substring(0, 16).getBytes(StandardCharsets.UTF_8);
    }
}
