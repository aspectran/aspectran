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
package com.aspectran.utils.security;

import com.aspectran.utils.Assert;
import com.aspectran.utils.PBEncryptionUtils;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;
import org.jspecify.annotations.Nullable;

/**
 * A utility for issuing and validating time-limited, password-based tokens.
 * <p>This class extends the functionality of {@link PBTokenIssuer} by embedding an
 * expiration timestamp within the token. The token's internal structure is
 * {@code expiration_timestamp_in_radix36 + "_" + payload}, which is then encrypted
 * using the settings from {@link PBEncryptionUtils}.
 * When parsing, it automatically validates the token's integrity and expiration time,
 * throwing an {@link ExpiredPBTokenException} if the token is past its expiry.</p>
 *
 * @see PBTokenIssuer
 * @see PBEncryptionUtils
 * @see ExpiredPBTokenException
 */
public final class TimeLimitedPBTokenIssuer {

    private static final String TOKEN_SEPARATOR = "_";

    private static final int DIGIT_RADIX = 36;

    private static final long DEFAULT_EXPIRATION_TIME = 1000L * 30;

    private static final Parameters EMPTY_PAYLOAD = new VariableParameters();

    private TimeLimitedPBTokenIssuer() {
    }

    /**
     * Returns a new time-limited token with an empty payload and the default expiration time (30 seconds).
     * This method is an alias for {@link #createToken()} and is typically used when calling as a static bean getter.
     * @return the encrypted token string
     */
    public static String getToken() {
        return createToken();
    }

    /**
     * Creates a new time-limited token with an empty payload and the default expiration time (30 seconds).
     * @return the encrypted token string
     */
    public static String createToken() {
        return createToken(EMPTY_PAYLOAD, DEFAULT_EXPIRATION_TIME);
    }

    /**
     * Creates a new time-limited token with an empty payload and the default expiration time (30 seconds)
     * using a specific encryption password.
     * @param encryptionPassword the password to use for encryption
     * @return the encrypted token string
     * @throws IllegalArgumentException if the encryptionPassword is null or empty
     */
    public static String createToken(String encryptionPassword) {
        return createToken(EMPTY_PAYLOAD, DEFAULT_EXPIRATION_TIME, encryptionPassword);
    }

    /**
     * Creates a new time-limited token with an empty payload.
     * @param expirationTime the expiration time in milliseconds
     * @return the encrypted token string
     */
    public static String createToken(long expirationTime) {
        return createToken(EMPTY_PAYLOAD, expirationTime);
    }

    /**
     * Creates a new time-limited token with an empty payload and a specific expiration time
     * using a specific encryption password.
     * @param expirationTime the expiration time in milliseconds
     * @param encryptionPassword the password to use for encryption
     * @return the encrypted token string
     * @throws IllegalArgumentException if the encryptionPassword is null or empty
     */
    public static String createToken(long expirationTime, String encryptionPassword) {
        return createToken(EMPTY_PAYLOAD, expirationTime, encryptionPassword);
    }

    /**
     * Creates a new time-limited token with the given payload and the default expiration time (30 seconds).
     * The payload is converted to an APON string and embedded in the token.
     * @param payload the parameters to be included in the token (must not be null)
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload is null
     */
    public static String createToken(Parameters payload) {
        return createToken(payload, DEFAULT_EXPIRATION_TIME);
    }

    /**
     * Creates a new time-limited token with the given payload and the default expiration time (30 seconds)
     * using a specific encryption password.
     * The payload is converted to an APON string and embedded in the token.
     * @param payload the parameters to be included in the token (must not be null)
     * @param encryptionPassword the password to use for encryption
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload or encryptionPassword is null or empty
     */
    public static String createToken(Parameters payload, String encryptionPassword) {
        return createToken(payload, DEFAULT_EXPIRATION_TIME, encryptionPassword);
    }

    /**
     * Creates a new time-limited token with the given payload and expiration time,
     * using the global encryption settings.
     * @param payload the parameters to be included in the token (must not be null)
     * @param expirationTime the expiration time in milliseconds from the current time
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload is null
     */
    public static String createToken(Parameters payload, long expirationTime) {
        Assert.notNull(payload, "payload must not be null");
        long time = System.currentTimeMillis() + expirationTime;
        String combined = Long.toString(time, DIGIT_RADIX) + TOKEN_SEPARATOR + payload;
        return PBEncryptionUtils.encrypt(combined);
    }

    /**
     * Creates a new time-limited token with the given payload and a specific expiration time
     * using a specific encryption password.
     * The payload is converted to an APON string and embedded in the token.
     * @param payload the parameters to be included in the token (must not be null)
     * @param expirationTime the expiration time in milliseconds from the current time
     * @param encryptionPassword the password to use for encryption
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload, or encryptionPassword is null or empty
     */
    public static String createToken(Parameters payload, long expirationTime, String encryptionPassword) {
        Assert.notNull(payload, "payload must not be null");
        Assert.hasLength(encryptionPassword, "encryptionPassword must not be null or empty");
        long time = System.currentTimeMillis() + expirationTime;
        String combined = Long.toString(time, DIGIT_RADIX) + TOKEN_SEPARATOR + payload;
        return PBEncryptionUtils.encrypt(combined, encryptionPassword);
    }

    /**
     * Parses the specified token using the global encryption settings and extracts the
     * payload as a {@link VariableParameters} instance.
     * This method validates both the token's integrity and its expiration time.
     * @param token the token string to parse
     * @param <T> the type of the payload, extending {@link Parameters}
     * @return the payload as a {@link VariableParameters} instance
     * @throws ExpiredPBTokenException if the token has expired
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     * @throws IllegalArgumentException if the token is null or empty
     */
    public static <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, (Class<T>)null);
    }

    /**
     * Parses the specified token using the global encryption settings and extracts the
     * payload into a new instance of the given type.
     * This method validates both the token's integrity and its expiration time.
     * @param token the token string to parse
     * @param payloadType the class of the payload, a subclass of {@link Parameters}.
     *      If null, a {@link VariableParameters} instance is returned.
     * @param <T> the type of the payload
     * @return a new instance of the specified payload type
     * @throws ExpiredPBTokenException if the token has expired
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     * @throws IllegalArgumentException if the token is null or empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends Parameters> T parseToken(String token, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        Assert.hasLength(token, "token must not be null or empty");
        long expirationTimeMillis;
        String payloadString;
        try {
            String combined = PBEncryptionUtils.decrypt(token);
            int index = combined.indexOf(TOKEN_SEPARATOR);
            if (index == -1) {
                throw new InvalidPBTokenException(token);
            }
            expirationTimeMillis = Long.parseLong(combined.substring(0, index), DIGIT_RADIX);
            payloadString = combined.substring(index + 1);
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
        if (expirationTimeMillis < System.currentTimeMillis()) {
            throw new ExpiredPBTokenException(token);
        }
        try {
            if (payloadType != null) {
                return AponReader.read(payloadString, payloadType);
            } else {
                return (T)AponReader.read(payloadString);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    /**
     * Parses the specified token and extracts the payload as {@link VariableParameters}
     * using a specific encryption password.
     * This method validates both the token's integrity and its expiration time.
     * @param token the token string to parse
     * @param encryptionPassword the password to use for decryption
     * @param <T> the type of the payload
     * @return the payload as a {@link VariableParameters} instance
     * @throws ExpiredPBTokenException if the token has expired
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    public static <T extends Parameters> T parseToken(String token, String encryptionPassword)
            throws InvalidPBTokenException {
        return parseToken(token, encryptionPassword, null);
    }

    /**
     * Parses the specified token and extracts the payload into a new instance of the given type
     * using a specific encryption password.
     * This method validates both the token's integrity and its expiration time.
     * @param token the token string to parse
     * @param encryptionPassword the password to use for decryption
     * @param payloadType the class of the payload, a subclass of {@link Parameters}
     * @param <T> the type of the payload
     * @return a new instance of the specified payload type
     * @throws ExpiredPBTokenException if the token has expired
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends Parameters> T parseToken(String token, String encryptionPassword, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        Assert.hasLength(token, "token must not be null or empty");
        Assert.hasLength(encryptionPassword, "encryptionPassword must not be null or empty");
        long expirationTimeMillis;
        String payloadString;
        try {
            String combined = PBEncryptionUtils.decrypt(token, encryptionPassword);
            int index = combined.indexOf(TOKEN_SEPARATOR);
            if (index == -1) {
                throw new InvalidPBTokenException(token);
            }
            expirationTimeMillis = Long.parseLong(combined.substring(0, index), DIGIT_RADIX);
            payloadString = combined.substring(index + 1);
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
        if (expirationTimeMillis < System.currentTimeMillis()) {
            throw new ExpiredPBTokenException(token);
        }
        try {
            if (payloadType != null) {
                return AponReader.read(payloadString, payloadType);
            } else {
                return (T)AponReader.read(payloadString);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    /**
     * Validates the given token by attempting to parse it.
     * If the token is invalid, expired, or malformed, an exception is thrown.
     * @param token the token to validate
     * @throws InvalidPBTokenException if the token is invalid or has expired
     */
    public static void validate(String token) throws InvalidPBTokenException {
        parseToken(token);
    }

    /**
     * Validates the given token by attempting to parse it with a specific encryption password.
     * If the token is invalid, expired, or malformed, an exception is thrown.
     * @param token the token to validate
     * @param encryptionPassword the password to use for decryption
     * @throws InvalidPBTokenException if the token is invalid or has expired
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    public static void validate(String token, String encryptionPassword) throws InvalidPBTokenException {
        parseToken(token, encryptionPassword, null);
    }

}
