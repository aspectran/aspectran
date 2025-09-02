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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.jasypt.encryption.ByteEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.iv.RandomIvGenerator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This class provides basic encryption/decryption capabilities to implement PBE.
 * The encryption algorithm and password can be configured via JVM system properties.
 * These properties are read only once when the class is loaded.
 *
 * <p>Note: Use {@link org.jasypt.util.password.StrongPasswordEncryptor} for
 * high-strength password digesting and checking.</p>
 *
 * <p>Created: 20/10/2018</p>
 *
 * @since 5.3.3
 */
public abstract class PBEncryptionUtils {

    /**
     * The default encryption algorithm is "PBEWITHHMACSHA256ANDAES_128".
     * This is a modern and secure password-based encryption algorithm recommended by security experts.
     * <p><strong>NOTE ON ENCRYPTED LENGTH:</strong> Modern algorithms like this one produce
     * longer encrypted strings compared to older ones (e.g., "PBEWithMD5AndTripleDES").
     * This is an intentional and necessary design for security. The increased length comes from
     * including a random salt and a random Initialization Vector (IV) with each encrypted message.
     * <ul>
     *   <li><b>Salt:</b> Prevents pre-computation attacks (like rainbow tables).</li>
     *   <li><b>IV:</b> Ensures that encrypting the same data multiple times produces different results.</li>
     * </ul>
     * Attempting to shorten the output by removing or fixing the salt/IV would severely
     * compromise security and is strongly discouraged. The longer string length is a
     * trade-off for significantly enhanced security. If high security is not a requirement
     * and reducing the length of the encrypted string is a higher priority, you can switch
     * to a legacy algorithm like {@code "PBEWithMD5AndTripleDES"} by setting the
     * "{@value #ENCRYPTION_ALGORITHM_KEY}" system property. However, be aware that this
     * will significantly reduce the security level.
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#pbecipher-algorithms">Java Security Standard Algorithm Names</a>
     */
    public static final String DEFAULT_ALGORITHM = "PBEWITHHMACSHA256ANDAES_128";

    /**
     * The name of the system property that specifies the encryption algorithm.
     */
    public static final String ENCRYPTION_ALGORITHM_KEY = "aspectran.encryption.algorithm";

    /**
     * The name of the system property that specifies the encryption password.
     */
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

    /**
     * Returns the encryption algorithm currently in use.
     * This value is determined from the "{@value #ENCRYPTION_ALGORITHM_KEY}" system property
     * at class loading time.
     * @return the name of the encryption algorithm
     */
    public static String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the encryption password currently in use.
     * This value is determined from the "{@value #ENCRYPTION_PASSWORD_KEY}" system property
     * at class loading time.
     * @return the encryption password, or {@code null} if not set
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Encrypts the input string using the default password-based encryptor.
     * The default password is configured via the "{@value #ENCRYPTION_PASSWORD_KEY}" system property.
     * @param inputString the string to encrypt
     * @return the result of encryption, as a URL-safe Base64-encoded string
     * @throws InsufficientEnvironmentException if the default encryption password is not set
     */
    public static String encrypt(String inputString) {
        return getDefaultEncryptor().encrypt(inputString);
    }

    /**
     * Decrypts the input string using the default password-based encryptor.
     * The default password is configured via the "{@value #ENCRYPTION_PASSWORD_KEY}" system property.
     * @param encryptedString the URL-safe Base64-encoded string to decrypt
     * @return the result of decryption
     * @throws InsufficientEnvironmentException if the default encryption password is not set
     */
    public static String decrypt(String encryptedString) {
        return getDefaultEncryptor().decrypt(encryptedString);
    }

    /**
     * Encrypts the input string using the specified password.
     * This method creates a temporary encryptor for the operation.
     * @param inputString the string to encrypt
     * @param encryptionPassword the password to be used for encryption
     * @return the result of encryption, as a URL-safe Base64-encoded string
     * @throws InsufficientEnvironmentException if the provided password is null or empty
     */
    public static String encrypt(String inputString, String encryptionPassword) {
        return getStringEncryptor(encryptionPassword).encrypt(inputString);
    }

    /**
     * Decrypts the input string using the specified password.
     * This method creates a temporary encryptor for the operation.
     * @param encryptedString the URL-safe Base64-encoded string to decrypt
     * @param encryptionPassword the password used for encryption
     * @return the result of decryption
     * @throws InsufficientEnvironmentException if the provided password is null or empty
     */
    public static String decrypt(String encryptedString, String encryptionPassword) {
        return getStringEncryptor(encryptionPassword).decrypt(encryptedString);
    }

    /**
     * Returns the default {@link StringEncryptor} instance.
     * <p>This encryptor is initialized at class loading time using the algorithm and password
     * specified by the "{@value #ENCRYPTION_ALGORITHM_KEY}" and "{@value #ENCRYPTION_PASSWORD_KEY}"
     * system properties.</p>
     * @return the default string encryptor
     * @throws InsufficientEnvironmentException if the encryption password is not set
     */
    public static StringEncryptor getDefaultEncryptor() {
        if (encryptor == null) {
            checkPassword(null);
        }
        return encryptor;
    }

    /**
     * Creates and returns a new {@link StringEncryptor} instance for the given password.
     * The algorithm used is the one configured via the system property.
     * @param encryptionPassword the password to be used for encryption/decryption
     * @return a new string encryptor instance
     * @throws InsufficientEnvironmentException if the provided password is null or empty
     */
    @NonNull
    public static StringEncryptor getStringEncryptor(String encryptionPassword) {
        return new CustomStringEncryptor(getByteEncryptor(encryptionPassword));
    }

    /**
     * Creates and returns a new {@link ByteEncryptor} instance for the given password.
     * <p>This method configures the underlying {@link StandardPBEByteEncryptor} with the
     * currently active algorithm. If the algorithm is AES-based, it automatically sets up
     * an {@link org.jasypt.iv.IvGenerator}.</p>
     * @param encryptionPassword the password to be used for encryption/decryption
     * @return a new byte encryptor instance
     * @throws InsufficientEnvironmentException if the provided password is null or empty
     */
    @NonNull
    public static ByteEncryptor getByteEncryptor(String encryptionPassword) {
        checkPassword(encryptionPassword);
        StandardPBEByteEncryptor byteEncryptor = new StandardPBEByteEncryptor();
        byteEncryptor.setAlgorithm(algorithm);
        byteEncryptor.setPassword(encryptionPassword);
        // AES algorithms require an IV (Initialization Vector)
        if (algorithm.contains("AES")) {
            byteEncryptor.setIvGenerator(new RandomIvGenerator());
        }
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
        @NonNull
        public String encrypt(String inputString) {
            Assert.notNull(inputString, "inputString must not be null");
            byte[] bytes = inputString.getBytes(MESSAGE_CHARSET);
            byte[] encrypted = encode(byteEncryptor.encrypt(bytes));
            return new String(encrypted, ENCRYPTED_MESSAGE_CHARSET);
        }

        @Override
        @NonNull
        public String decrypt(String encryptedString) {
            Assert.notNull(encryptedString, "encryptedString must not be null");
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
