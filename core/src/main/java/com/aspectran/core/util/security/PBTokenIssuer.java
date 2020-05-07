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
package com.aspectran.core.util.security;

import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Password based token issuer.
 */
public class PBTokenIssuer {

    public String createToken(Parameters payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        return encode(PBEncryptionUtils.encrypt(payload.toString()));
    }

    public <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Parameters> T parseToken(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        try {
            String payload = PBEncryptionUtils.decrypt(decode(token));
            if (payloadType != null) {
                return AponReader.parse(payload, payloadType);
            } else {
                return (T)AponReader.parse(payload);
            }
        } catch (IOException e) {
            throw new InvalidPBTokenException(token, e);
        }
    }

    public static String getToken(Parameters payload) {
        return new PBTokenIssuer().createToken(payload);
    }

    public static <T extends Parameters> T getPayload(String token)
            throws InvalidPBTokenException {
        return new PBTokenIssuer().parseToken(token);
    }

    public static <T extends Parameters> T getPayload(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        return new PBTokenIssuer().parseToken(token, payloadType);
    }

    protected String encode(String text) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    protected String decode(String text) {
        byte[] bytes = Base64.getUrlDecoder()
                .decode(text.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
