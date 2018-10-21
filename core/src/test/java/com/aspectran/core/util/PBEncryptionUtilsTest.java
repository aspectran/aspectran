package com.aspectran.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * <p>Created: 21/10/2018</p>
 */
public class PBEncryptionUtilsTest {

    private String oldPassword;
    private String oldAlgorithm;

    @Before
    public void saveProperties() {
        oldPassword = System.getProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
        oldAlgorithm = System.getProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_KEY);
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "abcd1234()");
    }

    @After
    public void restoreProperties() {
        if (oldPassword == null) {
            System.clearProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY);
        } else {
            System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, oldPassword);
        }
        if (oldAlgorithm == null) {
            System.clearProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_KEY);
        } else {
            System.setProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_KEY, oldAlgorithm);
        }
    }

    @Test
    public void testEncrypt() {
        String original = "1234"; // fmNbd3A/Jfey9jT+WzoOvQ==
        String encrypted = PBEncryptionUtils.encrypt(original);
        String decrypted = PBEncryptionUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testEncryptTriple() {
        System.setProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_KEY, "PBEWithMD5AndTripleDES");
        String original = "1234"; // X/GeIRH83RB8SX31cMM4jA==
        String encrypted = PBEncryptionUtils.encrypt(original);
        String decrypted = PBEncryptionUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

}