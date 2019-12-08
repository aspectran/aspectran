package com.aspectran.core.component.session.redis.lettuce;

/**
 * <p>Created: 2019/12/08</p>
 */
public class SessionDataSerializationException extends RuntimeException {

    private static final long serialVersionUID = -4113369748730396646L;

    public SessionDataSerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
