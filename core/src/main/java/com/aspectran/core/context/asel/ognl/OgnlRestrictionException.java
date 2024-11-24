package com.aspectran.core.context.asel.ognl;

import java.io.Serial;

/**
 * <p>Created: 2024. 11. 24.</p>
 */
public class OgnlRestrictionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8829028434827240355L;

    public OgnlRestrictionException(String message) {
        super(message);
    }

    public OgnlRestrictionException(String message, Throwable cause) {
        super(message, cause);
    }

}
