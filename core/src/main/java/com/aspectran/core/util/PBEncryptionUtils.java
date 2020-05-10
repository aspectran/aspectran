/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.context.InsufficientEnvironmentException;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

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

    private static final String algorithm;

    private static final String password;

    static {
        algorithm = StringUtils.trimWhitespace(SystemUtils.getProperty(ENCRYPTION_ALGORITHM_KEY, DEFAULT_ALGORITHM));
        password = StringUtils.trimWhitespace(SystemUtils.getProperty(ENCRYPTION_PASSWORD_KEY));
    }

    public static String getAlgorithm() {
        return algorithm;
    }

    public static String getPassword() {
        return password;
    }

    /**
     * Encrypts the inputString using the encryption password.
     *
     * @param inputString the string to encrypt
     * @return the encrypted string
     */
    public static String encrypt(String inputString) {
        return encrypt(inputString, password);
    }

    /**
     * Encrypts the inputString using the encryption password.
     *
     * @param inputString the string to encrypt
     * @param encryptionPassword the password to be used for encryption
     * @return the encrypted string
     */
    public static String encrypt(String inputString, String encryptionPassword) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString must not be null");
        }
        return encode(getEncryptor(encryptionPassword).encrypt(inputString));
    }

    /**
     * Decrypts the inputString using the encryption password.
     *
     * @param inputString the key used to originally encrypt the string
     * @return the decrypted version of inputString
     */
    public static String decrypt(String inputString) {
        return decrypt(inputString, password);
    }

    /**
     * Decrypts the inputString using the encryption password.
     *
     * @param inputString the key used to originally encrypt the string
     * @param encryptionPassword the password to be used for encryption
     * @return the decrypted version of inputString
     */
    public static String decrypt(String inputString, String encryptionPassword) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString must not be null");
        }
        checkPassword(encryptionPassword);
        return getEncryptor(encryptionPassword).decrypt(decode(inputString));
    }

    private static String encode(String inputString) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString must not be null");
        }
        if (inputString.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(inputString.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String inputString) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString must not be null");
        }
        if (inputString.isEmpty()) {
            return StringUtils.EMPTY;
        }
        byte[] bytes = Base64.getUrlDecoder()
                .decode(inputString.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static PBEStringEncryptor getEncryptor() {
        return getEncryptor(password);
    }

    public static PBEStringEncryptor getEncryptor(String encryptionPassword) {
        checkPassword(encryptionPassword);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(algorithm);
        encryptor.setPassword(encryptionPassword);
        return encryptor;
    }

    private static void checkPassword(String encryptionPassword) {
        if (!StringUtils.hasText(encryptionPassword)) {
            throw new InsufficientEnvironmentException("A password is required to attempt password-based encryption " +
                    "or decryption; Make sure the JVM system property \"aspectran.encryption.password\" is set up; " +
                    "(Default algorithm: " + getAlgorithm() + ")");
        }
    }

}
