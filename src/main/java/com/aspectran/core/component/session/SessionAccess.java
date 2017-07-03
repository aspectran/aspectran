package com.aspectran.core.component.session;

/**
 * <p>Created: 2017. 7. 2.</p>
 */
public interface SessionAccess {

    /**
     * Called by the {@link com.aspectran.core.activity.CoreActivity} when a session is first accessed by a request.
     */
    void access();

    void complete();

}
