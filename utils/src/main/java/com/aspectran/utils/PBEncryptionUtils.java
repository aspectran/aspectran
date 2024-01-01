/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.Nullable;
import org.jasypt.encryption.ByteEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class provides basic encryption/decryption capabilities to implement PBE.
 *
 * <p>Note: Note: Use {@link org.jasypt.util.password.StrongPasswordEncryptor} for
 * high-strength password digesting and checking.</p>
 *
 * <p>Created: 20/10/2018</p>
 *
 * @since 5.3.3
 */
public class PBEncryptionUtils {

    public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndTripleDES";

    public static final String ENCRYPTION_ALGORITHM_KEY = "aspectran.encryption.algorithm";

    public static final String ENCRYPTION_PASSWORD_KEY = "aspectran.encryption.password";

    private static final Charset MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private static final Charset ENCRYPTED_MESSAGE_CHARSET = StandardCharsets.US_ASCII;

    private static final String algorithm;

    private static final String password;

    private static final StringEncryptor encryptor;

    static {
        algorithm = StringUtils.trimWhitespace(SystemUtils.getProperty(ENCRYPTION_ALGORITHM_KEY, DEFAULT_ALGORITHM));
        password = StringUtils.trimWhitespace(SystemUtils.getProperty(ENCRYPTION_PASSWORD_KEY));
        encryptor = (StringUtils.hasText(password) ? getStringEncryptor(password) : null);
    }

    public static String getAlgorithm() {
        return algorithm;
    }

    public static String getPassword() {
        return password;
    }

    /**
     * Encrypts the inputString using the encryption password.
     * @param inputString the string to encrypt
     * @return the result of encryption
     */
    public static String encrypt(String inputString) {
        return encryptor.encrypt(inputString);
    }

    /**
     * Decrypts the inputString using the encryption password.
     * @param encryptedString the string to decrypt
     * @return the result of decryption
     */
    public static String decrypt(String encryptedString) {
        return encryptor.decrypt(encryptedString);
    }

    /**
     * Encrypts the inputString using the encryption password.
     * @param inputString the string to encrypt
     * @param encryptionPassword the password to be used for encryption
     * @return the result of encryption
     */
    public static String encrypt(String inputString, String encryptionPassword) {
        ByteEncryptor byteEncryptor = getByteEncryptor(encryptionPassword);
        StringEncryptor stringEncryptor = new CustomStringEncryptor(byteEncryptor);
        return stringEncryptor.encrypt(inputString);
    }

    /**
     * Decrypts the inputString using the encryption password.
     * @param encryptedString the string to decrypt
     * @param encryptionPassword the password used for encryption
     * @return the result of decryption
     */
    public static String decrypt(String encryptedString, String encryptionPassword) {
        ByteEncryptor byteEncryptor = getByteEncryptor(encryptionPassword);
        StringEncryptor stringEncryptor = new CustomStringEncryptor(byteEncryptor);
        return stringEncryptor.decrypt(encryptedString);
    }

    public static StringEncryptor getDefaultEncryptor() {
        if (encryptor == null) {
            checkPassword(null);
        }
        return encryptor;
    }

    public static StringEncryptor getStringEncryptor(String encryptionPassword) {
        return new CustomStringEncryptor(getByteEncryptor(encryptionPassword));
    }

    public static ByteEncryptor getByteEncryptor(String encryptionPassword) {
        checkPassword(encryptionPassword);
        StandardPBEByteEncryptor byteEncryptor = new StandardPBEByteEncryptor();
        byteEncryptor.setAlgorithm(algorithm);
        byteEncryptor.setPassword(encryptionPassword);
        return byteEncryptor;
    }

    private static void checkPassword(@Nullable String encryptionPassword) {
        if (!StringUtils.hasText(encryptionPassword)) {
            throw new InsufficientEnvironmentException("A password is required to attempt password-based encryption " +
                    "or decryption; Make sure the JVM system property \"" + ENCRYPTION_PASSWORD_KEY + "\" is set up; " +
                    "(Default algorithm: " + getAlgorithm() + ")");
        }
    }

    /**
     * Aspectran friendly string encryptor.
     */
    private static final class CustomStringEncryptor implements StringEncryptor {

        private final ByteEncryptor byteEncryptor;

        public CustomStringEncryptor(ByteEncryptor byteEncryptor) {
            this.byteEncryptor = byteEncryptor;
        }

        @Override
        public String encrypt(String inputString) {
            if (inputString == null) {
                throw new IllegalArgumentException("inputString must not be null");
            }
            byte[] bytes = inputString.getBytes(MESSAGE_CHARSET);
            byte[] encrypted = encode(byteEncryptor.encrypt(bytes));
            return new String(encrypted, ENCRYPTED_MESSAGE_CHARSET);
        }

        @Override
        public String decrypt(String encryptedString) {
            if (encryptedString == null) {
                throw new IllegalArgumentException("encryptedString must not be null");
            }
            byte[] encrypted = decode(encryptedString.getBytes(ENCRYPTED_MESSAGE_CHARSET));
            byte[] decrypted = byteEncryptor.decrypt(encrypted);
            return new String(decrypted, MESSAGE_CHARSET);
        }

        private byte[] encode(byte[] inputBytes) {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encode(inputBytes);
        }

        private byte[] decode(byte[] inputBytes) {
            return Base64.getUrlDecoder()
                    .decode(inputBytes);
        }

    }

}
