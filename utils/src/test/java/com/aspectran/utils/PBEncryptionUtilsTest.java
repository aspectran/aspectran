/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test cases for PBEncryptionUtils with various algorithms and salt configurations.
 *
 * <p>Created: 21/10/2018</p>
 */
class PBEncryptionUtilsTest {

    private final String[] algorithms = {
            "PBEWithMD5AndTripleDES",
            "PBEWithSHA1AndDESede",
            "PBEWITHHMACSHA256ANDAES_128",
            "PBEWITHHMACSHA512ANDAES_256"
    };

    @AfterAll
    static void restoreProperties() {
        System.clearProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_PROPERTY);
        System.clearProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_PROPERTY);
        System.clearProperty(PBEncryptionUtils.ENCRYPTION_SALT_PROPERTY);
        PBEncryptionUtils.reload();
    }

    @Test
    void testVariousAlgorithmsWithRandomSalt() {
        String original = "Aspectow-Secret-Data-12345";
        String password = "shared-cluster-password";

        for (String algo : algorithms) {
            try {
                StringEncryptor encryptor = PBEncryptionUtils.getStringEncryptor(algo, password, null);

                String encrypted = encryptor.encrypt(original);
                String decrypted = encryptor.decrypt(encrypted);

                assertEquals(original, decrypted, "Decryption failed for algorithm: " + algo);

                // Random salt should produce different results each time
                assertNotEquals(encrypted, encryptor.encrypt(original),
                        "Random salt should produce different outputs: " + algo);
            } catch (Exception e) {
                if (algo.contains("256")) {
                    System.err.println("Skipping AES-256 test: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        }
    }

    @Test
    void testVariousAlgorithmsWithFixedSalt() {
        String original = "Aspectow-Secret-Data-12345";
        String password = "shared-cluster-password";
        String salt = "this-is-a-very-long-fixed-salt-for-testing";

        for (String algo : algorithms) {
            try {
                StringEncryptor encryptor = PBEncryptionUtils.getStringEncryptor(algo, password, salt);

                String encrypted1 = encryptor.encrypt(original);
                String encrypted2 = encryptor.encrypt(original);
                String decrypted = encryptor.decrypt(encrypted1);

                assertEquals(original, decrypted, "Decryption failed for algorithm: " + algo);

                if (algo.contains("AES")) {
                    // AES algorithms use RandomIvGenerator, so output is non-deterministic
                    // even if the salt is fixed.
                    assertNotEquals(encrypted1, encrypted2,
                            "AES should produce different outputs due to Random IV even with fixed salt: " + algo);
                } else {
                    // Legacy algorithms without IV should be deterministic with fixed salt
                    assertEquals(encrypted1, encrypted2,
                            "Legacy algorithm should be deterministic with fixed salt: " + algo);
                }
            } catch (Exception e) {
                if (algo.contains("256")) {
                    System.err.println("Skipping AES-256 test: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        }
    }

    @Test
    void testSystemPropertySalt() {
        String original = "Aspectow-Secret-Data";
        String password = "system-password";
        String salt = "system-fixed-salt-value";

        System.setProperty(PBEncryptionUtils.ENCRYPTION_ALGORITHM_PROPERTY, "PBEWithMD5AndTripleDES");
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_PROPERTY, password);
        System.setProperty(PBEncryptionUtils.ENCRYPTION_SALT_PROPERTY, salt);
        PBEncryptionUtils.reload();

        // Now PBEncryptionUtils should use the updated properties.
        String encrypted = PBEncryptionUtils.encrypt(original);
        String decrypted = PBEncryptionUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

}
