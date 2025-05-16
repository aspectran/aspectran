package com.aspectran.core.activity.response;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>Created: 2025-05-16</p>
 */
public class RedirectTarget {

    private final String requestName;

    private final String location;

    private RedirectTarget(String requestName, String location) {
        this.requestName = requestName;
        this.location = location;
    }

    public String getRequestName() {
        return requestName;
    }

    public String getLocation() {
        return location;
    }

    @NonNull
    public static RedirectTarget of(String requestName, String location) {
        Assert.notNull(requestName, "requestName must not be null");
        return new RedirectTarget(requestName, location);
    }

}
