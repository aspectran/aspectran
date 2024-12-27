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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.SessionData;
import io.lettuce.core.codec.RedisCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * {@link SessionDataCodec} encodes the session data sent to Redis,
 * and decodes the session data in the command output.
 *
 * <p>Created: 2019/12/08</p>
 */
public class SessionDataCodec implements RedisCodec<String, SessionData> {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final Set<String> nonPersistentAttributes;

    public SessionDataCodec(Set<String> nonPersistentAttributes) {
        this.nonPersistentAttributes = nonPersistentAttributes;
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return UTF8.decode(bytes).toString();
    }

    @Override
    public SessionData decodeValue(ByteBuffer bytes) {
        try {
            byte[] array = new byte[bytes.remaining()];
            bytes.get(array);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(array);
            return SessionData.deserialize(inputStream);
        } catch (Exception e) {
            throw new SessionDataSerializationException("Error decoding session data", e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return UTF8.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(SessionData value) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SessionData.serialize(value, outputStream, nonPersistentAttributes);
            return ByteBuffer.wrap(outputStream.toByteArray());
        } catch (IOException e) {
            throw new SessionDataSerializationException("Error encoding session data", e);
        }
    }

}
