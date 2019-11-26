/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

/**
 * Time-limited, password based token issuer.
 */
public class TimeLimitedPBTokenIssuer extends PBTokenIssuer {

    private static final String PAYLOAD_SEPARATOR = "_";

    private static final long DEFAULT_EXPIRATION_TIME = 1000 * 30;

    private final long expirationTime;

    public TimeLimitedPBTokenIssuer() {
        this(DEFAULT_EXPIRATION_TIME);
    }

    public TimeLimitedPBTokenIssuer(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String createToken(Parameters payload) {
        long time = System.currentTimeMillis() + expirationTime;
        String encodedTime = encode(PBEncryptionUtils.encrypt(Long.toString(time)));
        if (payload != null) {
            String combined = encodedTime + PAYLOAD_SEPARATOR +
                    encode(PBEncryptionUtils.encrypt(payload.toString()));
            return encode(combined);
        } else {
            return encodedTime;
        }
    }

    @Override
    public <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T parseToken(String token, Class<T> payloadType) throws InvalidPBTokenException {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        long time;
        String payload;
        try {
            String[] arr = StringUtils.split(decode(token), PAYLOAD_SEPARATOR);
            if (arr.length == 2) {
                time = Long.parseLong(PBEncryptionUtils.decrypt(decode(arr[0])));
                payload = PBEncryptionUtils.decrypt(decode(arr[1]));
            } else if (arr.length == 1) {
                time = Long.parseLong(PBEncryptionUtils.decrypt(arr[0]));
                payload = null;
            } else {
                throw new InvalidPBTokenException(token);
            }
        } catch (Exception e) {
            throw new InvalidPBTokenException(token, e);
        }
        if (time < System.currentTimeMillis()) {
            throw new ExpiredPBTokenException(token);
        }
        if (payload != null) {
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
        return null;
    }

    public static String getToken(Parameters payload) {
        return new TimeLimitedPBTokenIssuer().createToken(payload);
    }

    public static <T extends Parameters> T getPayload(String token)
            throws InvalidPBTokenException {
        return new TimeLimitedPBTokenIssuer().parseToken(token);
    }

    public static <T extends Parameters> T getPayload(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        return new TimeLimitedPBTokenIssuer().parseToken(token, payloadType);
    }

    public static String getToken() {
        return new TimeLimitedPBTokenIssuer().createToken(null);
    }

    public static void validate(String token) throws InvalidPBTokenException {
        new TimeLimitedPBTokenIssuer().parseToken(token, null);
    }

}
