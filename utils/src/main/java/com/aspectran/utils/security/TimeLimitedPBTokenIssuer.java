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

import com.aspectran.utils.PBEncryptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

/**
 * Issues and validates time-limited, password-based tokens.
 * <p>The generated token has an expiration time and is encrypted using the password-based
 * encryption configured in {@link PBEncryptionUtils}. The token format is
 * {@code encrypt(expiration_timestamp_in_radix36 + "_" + payload)}.
 * The payload is an APON (Aspectran Parameter Object Notation) string derived from a
 * {@link Parameters} object.</p>
 */
public class TimeLimitedPBTokenIssuer extends PBTokenIssuer {

    private static final String TOKEN_SEPARATOR = "_";

    private static final Parameters EMPTY_PAYLOAD = new VariableParameters();

    private static final int DIGIT_RADIX = 36;

    private static final long DEFAULT_EXPIRATION_TIME = 1000 * 30;

    private static final TimeLimitedPBTokenIssuer defaultIssuer = new TimeLimitedPBTokenIssuer();

    private final long expirationTime;

    /**
     * Creates a new token issuer with the default expiration time (30 seconds).
     */
    public TimeLimitedPBTokenIssuer() {
        this(DEFAULT_EXPIRATION_TIME);
    }

    /**
     * Creates a new token issuer with the specified expiration time.
     * @param expirationTime the expiration time in milliseconds
     */
    public TimeLimitedPBTokenIssuer(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Creates a new time-limited token with an empty payload.
     * @return the encrypted token string
     */
    public String createToken() {
        return createToken(EMPTY_PAYLOAD);
    }

    /**
     * Creates a new time-limited token with the given payload.
     * The payload is converted to an APON string and embedded in the token.
     * @param payload the parameters to be included in the token (must not be null)
     * @return the encrypted token string
     * @throws IllegalArgumentException if the payload is null
     */
    @Override
    public String createToken(Parameters payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        long time = System.currentTimeMillis() + expirationTime;
        String combined = Long.toString(time, DIGIT_RADIX) + TOKEN_SEPARATOR + payload;
        return PBEncryptionUtils.encrypt(combined);
    }

    /**
     * Parses the specified token and extracts the payload as {@link VariableParameters}.
     * This method validates the token's integrity and expiration time.
     * @param token the token string to parse
     * @return the payload as a {@link VariableParameters} instance
     * @throws InvalidPBTokenException if the token is invalid, malformed, or expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    @Override
    public <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, null);
    }

    /**
     * Parses the specified token and extracts the payload into a new instance of the given type.
     * This method validates the token's integrity and expiration time.
     * @param token the token string to parse
     * @param payloadType the class of the payload, a subclass of {@link Parameters}
     * @return a new instance of the specified payload type
     * @throws InvalidPBTokenException if the token is invalid, malformed, or expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T parseToken(String token, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("token must not be null or empty");
        }
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
     * A static helper method to get a new token with the default expiration time and an empty payload.
     * @return the encrypted token string
     */
    public static String getToken() {
        return defaultIssuer.createToken();
    }

    /**
     * A static helper method to get a new token with the specified expiration time and an empty payload.
     * @param expirationTime the expiration time in milliseconds
     * @return the encrypted token string
     */
    public static String getToken(long expirationTime) {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(expirationTime);
        return tokenIssuer.createToken();
    }

    /**
     * A static helper method to get a new token with the given payload and the default expiration time.
     * @param payload the parameters to be included in the token
     * @return the encrypted token string
     */
    public static String getToken(Parameters payload) {
        return defaultIssuer.createToken(payload);
    }

    /**
     * A static helper method to get a new token with the given payload and the specified expiration time.
     * @param payload the parameters to be included in the token
     * @param expirationTime the expiration time in milliseconds
     * @return the encrypted token string
     */
    public static String getToken(Parameters payload, long expirationTime) {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(expirationTime);
        return tokenIssuer.createToken(payload);
    }

    /**
     * A static helper method to parse a token and get its payload.
     * @param token the token string to parse
     * @return the payload as a {@link VariableParameters} instance
     * @throws InvalidPBTokenException if the token is invalid, malformed, or expired
     */
    public static <T extends Parameters> T getPayload(String token)
            throws InvalidPBTokenException {
        return getPayload(token, null);
    }

    /**
     * A static helper method to parse a token and get its payload as the specified type.
     * @param token the token string to parse
     * @param payloadType the class of the payload, a subclass of {@link Parameters}
     * @return a new instance of the specified payload type
     * @throws InvalidPBTokenException if the token is invalid, malformed, or expired
     */
    public static <T extends Parameters> T getPayload(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(0L);
        return tokenIssuer.parseToken(token, payloadType);
    }

    /**
     * A static helper method to validate a token.
     * It checks the token's integrity and expiration time without returning the payload.
     * @param token the token string to validate
     * @throws InvalidPBTokenException if the token is invalid, malformed, or expired
     */
    public static void validate(String token) throws InvalidPBTokenException {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(0L);
        tokenIssuer.parseToken(token, null);
    }

}
