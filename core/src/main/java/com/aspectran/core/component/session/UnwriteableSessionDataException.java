package com.aspectran.core.component.session;

import com.aspectran.core.context.AspectranCheckedException;

/**
 * <p>Created: 2017. 9. 27.</p>
 */
public class UnwriteableSessionDataException extends AspectranCheckedException {
    private String id;

    public UnwriteableSessionDataException(String id, Throwable t) {
        super("Unwriteable session " + id, t);
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

}
