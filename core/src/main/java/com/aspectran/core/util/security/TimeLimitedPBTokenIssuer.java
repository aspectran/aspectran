/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.util.security;

import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

/**
 * Time-limited, password based token issuer.
 */
public class TimeLimitedPBTokenIssuer extends PBTokenIssuer {

    private static final String TOKEN_SEPARATOR = "_";

    private static final Parameters EMPTY_PAYLOAD = new VariableParameters();

    private static final int DIGIT_RADIX = 36;

    private static final long DEFAULT_EXPIRATION_TIME = 1000 * 30;

    private final long expirationTime;

    public TimeLimitedPBTokenIssuer() {
        this(DEFAULT_EXPIRATION_TIME);
    }

    public TimeLimitedPBTokenIssuer(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String createToken() {
        return createToken(EMPTY_PAYLOAD);
    }

    @Override
    public String createToken(Parameters payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        long time = System.currentTimeMillis() + expirationTime;
        String combined = Long.toString(time, DIGIT_RADIX) + TOKEN_SEPARATOR + payload.toString();
        return PBEncryptionUtils.encrypt(combined);
    }

    @Override
    public <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T parseToken(String token, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("token must not be null or empty");
        }
        long time;
        String payload;
        try {
            String combined = PBEncryptionUtils.decrypt(token);
            int index = combined.indexOf(TOKEN_SEPARATOR);
            if (index == -1) {
                throw new InvalidPBTokenException(token);
            }
            time = Long.parseLong(combined.substring(0, index), DIGIT_RADIX);
            payload = combined.substring(index + 1);
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
        if (time < System.currentTimeMillis()) {
            throw new ExpiredPBTokenException(token);
        }
        try {
            if (payloadType != null) {
                return AponReader.parse(payload, payloadType);
            } else {
                return (T)AponReader.parse(payload);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    public static String getToken() {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer();
        return tokenIssuer.createToken();
    }

    public static String getToken(long expirationTime) {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(expirationTime);
        return tokenIssuer.createToken();
    }

    public static String getToken(Parameters payload) {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer();
        return tokenIssuer.createToken(payload);
    }

    public static String getToken(Parameters payload, long expirationTime) {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(expirationTime);
        return tokenIssuer.createToken(payload);
    }

    public static <T extends Parameters> T getPayload(String token)
            throws InvalidPBTokenException {
        return getPayload(token, null);
    }

    public static <T extends Parameters> T getPayload(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(0L);
        return tokenIssuer.parseToken(token, payloadType);
    }

    public static void validate(String token) throws InvalidPBTokenException {
        TimeLimitedPBTokenIssuer tokenIssuer = new TimeLimitedPBTokenIssuer(0L);
        tokenIssuer.parseToken(token, null);
    }

}
