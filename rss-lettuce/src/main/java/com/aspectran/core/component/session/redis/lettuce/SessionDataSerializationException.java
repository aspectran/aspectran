package com.aspectran.core.component.session.redis.lettuce;

/**
 * Exception thrown when an error occurs during serialization or
 * deserialization of session data.
 *
 * <p>Created: 2019/12/08</p>
 */
public class SessionDataSerializationException extends RuntimeException {

    private static final long serialVersionUID = -4113369748730396646L;

    public SessionDataSerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
