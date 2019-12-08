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
 * <p>Created: 2019/12/08</p>
 */
public class SessionDataCodec implements RedisCodec<String, SessionData> {

    private static final Charset charset = StandardCharsets.UTF_8;

    private final Set<String> nonPersistentAttributes;

    public SessionDataCodec(Set<String> nonPersistentAttributes) {
        this.nonPersistentAttributes = nonPersistentAttributes;
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return charset.decode(bytes).toString();
    }

    @Override
    public SessionData decodeValue(ByteBuffer bytes) {
        try {
            byte[] array = new byte[bytes.remaining()];
            bytes.get(array);
            ByteArrayInputStream bais = new ByteArrayInputStream(array);
            return SessionData.deserialize(bais);
        } catch (Exception e) {
            throw new SessionDataSerializationException("Error decoding session data", e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return charset.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(SessionData value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SessionData.serialize(value, baos, nonPersistentAttributes);
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            throw new SessionDataSerializationException("Error encoding session data", e);
        }
    }

}
