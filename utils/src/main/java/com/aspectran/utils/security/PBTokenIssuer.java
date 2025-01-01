/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

/**
 * Password based token issuer.
 */
public class PBTokenIssuer {

    public String createToken(Parameters payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        return PBEncryptionUtils.encrypt(payload.toString());
    }

    public <T extends Parameters> T parseToken(String token) throws InvalidPBTokenException {
        return parseToken(token, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Parameters> T parseToken(String token, @Nullable Class<T> payloadType)
            throws InvalidPBTokenException {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("token must not be null or empty");
        }
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

    public static String getToken(Parameters payload) {
        PBTokenIssuer tokenIssuer = new PBTokenIssuer();
        return tokenIssuer.createToken(payload);
    }

    public static <T extends Parameters> T getPayload(String token)
            throws InvalidPBTokenException {
        return getPayload(token, null);
    }

    public static <T extends Parameters> T getPayload(String token, Class<T> payloadType)
            throws InvalidPBTokenException {
        PBTokenIssuer tokenIssuer = new PBTokenIssuer();
        return tokenIssuer.parseToken(token, payloadType);
    }

}
