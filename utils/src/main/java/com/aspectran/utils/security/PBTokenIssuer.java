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
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.Parameters;

/**
 * A utility for issuing and validating password-based tokens without an expiration time.
 * <p>This class encrypts a {@link Parameters} object into a secure token string.
 * It relies on the global encryption settings configured in {@link PBEncryptionUtils}
 * but also provides methods to use a specific password for each operation.
 * Unlike {@link TimeLimitedPBTokenIssuer}, tokens created by this issuer are perpetual
 * and must be invalidated by other means.</p>
 *
 * @see PBEncryptionUtils
 * @see TimeLimitedPBTokenIssuer
 * @see InvalidPBTokenException
 */
public final class PBTokenIssuer {

    private PBTokenIssuer() {
    }

    /**
     * Creates a new token by encrypting the given payload using the global encryption settings.
     * @param payload the parameters to be included in the token (must not be null)
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload is null
     */
    public static String createToken(Parameters payload) {
        Assert.notNull(payload, "payload must not be null");
        return PBEncryptionUtils.encrypt(payload.toString());
    }

    /**
     * Creates a new token by encrypting the given payload with a specific encryption password.
     * @param payload the parameters to be included in the token (must not be null)
     * @param encryptionPassword the password to use for encryption
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload or encryptionPassword is null or empty
     */
    public static String createToken(Parameters payload, String encryptionPassword) {
        Assert.notNull(payload, "payload must not be null");
        Assert.hasLength(encryptionPassword, "encryptionPassword must not be null or empty");
        return PBEncryptionUtils.encrypt(payload.toString(), encryptionPassword);
    }

    /**
     * Parses the specified token using the global encryption settings and extracts the
     * payload as a {@link com.aspectran.utils.apon.VariableParameters} instance.
     * @param token the token string to parse
     * @param <T> the type of the payload, extending {@link Parameters}
     * @return the payload as a {@link com.aspectran.utils.apon.VariableParameters} instance
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     */
    public static <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, (Class<T>)null);
    }

    /**
     * Parses the specified token using the global encryption settings and extracts the
     * payload into a new instance of the given type.
     * @param token the token string to parse (must not be null or empty)
     * @param payloadType the class of the payload, a subclass of {@link Parameters}.
     *      If null, a {@link com.aspectran.utils.apon.VariableParameters} instance is returned.
     * @param <T> the type of the payload
     * @return a new instance of the specified payload type
     * @throws InvalidPBTokenException if the token is invalid, malformed, or cannot be decrypted
     * @throws IllegalArgumentException if the token is null or empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends Parameters> T parseToken(String token, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        Assert.hasLength(token, "token must not be null or empty");
        try {
            String payload = PBEncryptionUtils.decrypt(token);
            if (payloadType != null) {
                return AponReader.read(payload, payloadType);
            } else {
                return (T)AponReader.read(payload);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    /**
     * Parses the specified token and extracts the payload as {@link com.aspectran.utils.apon.VariableParameters}
     * using a specific encryption password.
     * @param token the token string to parse
     * @param encryptionPassword the password to use for decryption
     * @param <T> the type of the payload
     * @return the payload as a {@link com.aspectran.utils.apon.VariableParameters} instance
     * @throws InvalidPBTokenException if the token is invalid or malformed
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    public static <T extends Parameters> T parseToken(String token, String encryptionPassword)
            throws InvalidPBTokenException {
        return parseToken(token, encryptionPassword, null);
    }

    /**
     * Parses the specified token and extracts the payload into a new instance of the given type
     * using a specific encryption password.
     * @param token the token string to parse
     * @param encryptionPassword the password to use for decryption
     * @param payloadType the class of the payload, a subclass of {@link Parameters}
     * @param <T> the type of the payload
     * @return a new instance of the specified payload type
     * @throws InvalidPBTokenException if the token is invalid or malformed
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    @SuppressWarnings("unchecked")
    public static <T extends Parameters> T parseToken(String token, String encryptionPassword, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        Assert.hasLength(token, "token must not be null or empty");
        Assert.hasLength(encryptionPassword, "encryptionPassword must not be null or empty");
        try {
            String payload = PBEncryptionUtils.decrypt(token, encryptionPassword);
            if (payloadType != null) {
                return AponReader.read(payload, payloadType);
            } else {
                return (T)AponReader.read(payload);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    /**
     * Validates the given token by attempting to parse it using the global encryption settings.
     * An exception is thrown if the token is invalid or malformed.
     * @param token the token to validate
     * @throws InvalidPBTokenException if the token is invalid
     */
    public static void validate(String token) throws InvalidPBTokenException {
        parseToken(token);
    }

    /**
     * Validates the given token by attempting to parse it with a specific encryption password.
     * An exception is thrown if the token is invalid or malformed.
     * @param token the token to validate
     * @param encryptionPassword the password to use for decryption
     * @throws InvalidPBTokenException if the token is invalid
     * @throws IllegalArgumentException if the token or encryptionPassword is null or empty
     */
    public static void validate(String token, String encryptionPassword) throws InvalidPBTokenException {
        parseToken(token, encryptionPassword);
    }

}
