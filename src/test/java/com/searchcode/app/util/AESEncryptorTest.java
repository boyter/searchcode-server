package com.searchcode.app.util;


import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AESEncryptorTest extends TestCase {

    public void testCreateWithRandomKeysWorks() throws Exception {
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            AESEncryptor aesEncryptor = new AESEncryptor(RandomStringUtils.randomAscii(rand.nextInt(64) + 1));
            aesEncryptor.encrypt("some text".getBytes());
            assertThat(aesEncryptor.decryptFromBase64String(aesEncryptor.encryptToBase64String("some text"))).isEqualTo("some text");
        }
    }

    public void testEncryptBytes() throws Exception {
        AESEncryptor aesEncryptor = new AESEncryptor();
        byte[] encrypt = aesEncryptor.encrypt("some text".getBytes());
        byte[] decrypt = aesEncryptor.decrypt(encrypt);

        assertThat(encrypt).isNotEqualTo(decrypt);
        assertThat(decrypt).isEqualTo("some text".getBytes());
    }

    public void testEncryptBase64() throws Exception {
        AESEncryptor aesEncryptor = new AESEncryptor();
        String encrypt = aesEncryptor.encryptToBase64String("a base64 encrypted thing");
        String decrypt = aesEncryptor.decryptFromBase64String(encrypt);

        assertThat(encrypt).isNotEqualTo(decrypt);
        assertThat(decrypt).isEqualTo("a base64 encrypted thing");
    }

}
