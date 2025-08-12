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

    /**
     * Creates a codec for String keys and {@link SessionData} values.
     * @param nonPersistentAttributes attribute names to exclude from serialization; may be {@code null}
     */
    public SessionDataCodec(Set<String> nonPersistentAttributes) {
        this.nonPersistentAttributes = nonPersistentAttributes;
    }

    /**
     * Decodes a String key from UTF-8 bytes.
     */
    @Override
    public String decodeKey(ByteBuffer bytes) {
        return UTF8.decode(bytes).toString();
    }

    /**
     * Decodes {@link SessionData} from a ByteBuffer.
     * @throws SessionDataSerializationException if deserialization fails
     */
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

    /**
     * Encodes a String key as UTF-8 bytes.
     */
    @Override
    public ByteBuffer encodeKey(String key) {
        return UTF8.encode(key);
    }

    /**
     * Encodes {@link SessionData} into a ByteBuffer using {@link SessionData#serialize} and
     * excluding any attributes configured as non-persistent.
     * @throws SessionDataSerializationException if serialization fails
     */
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
