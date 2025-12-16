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
package com.aspectran.core.activity;

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.HashMap;

/**
 * A FlashMap provides a way for one request to store attributes intended for
 * use in another. This is most commonly needed when redirecting from one URL
 * to another -- for example, the Post/Redirect/Get pattern. A FlashMap is saved before
 * the redirect (typically in the session) and is made available after the
 * redirect and removed immediately.
 *
 * <p>A FlashMap can be set up with a request path and request parameters to
 * help identify the target request. Without this information, a FlashMap is
 * made available to the next request, which may or may not be the intended
 * recipient. On a redirect, the target URL is known and a FlashMap can be
 * updated with that information. This is done automatically when the
 * {@code org.springframework.web.servlet.view.RedirectView} is used.
 *
 * <p>Note: annotated controllers will usually not use FlashMap directly.
 * See {@code org.springframework.web.servlet.mvc.support.RedirectAttributes}
 * for an overview of using flash attributes in annotated controllers.
 * @see FlashMapManager
 * @since 8.4.0
 */
public final class FlashMap extends HashMap<String, Object> implements Comparable<FlashMap> {

    @Serial
    private static final long serialVersionUID = -4459013870858137422L;

    @Nullable
    private String targetRequestName;

    private long expirationTime = -1;

    /**
     * Return the target URL path (or {@code null} if none specified).
     */
    @Nullable
    public String getTargetRequestName() {
        return targetRequestName;
    }

    /**
     * Provide a Translet name to help identify the target request for this FlashMap.
     */
    public void setTargetRequestName(@Nullable String requestName) {
        this.targetRequestName = requestName;
    }

    /**
     * Return the expiration time for the FlashMap or -1 if the expiration
     * period has not started.
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Set the expiration time for the FlashMap. This is provided for serialization
     * purposes but can also be used instead {@link #startExpirationPeriod(int)}.
     */
    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Start the expiration period for this instance.
     * @param timeToLive the number of seconds before expiration
     */
    public void startExpirationPeriod(int timeToLive) {
        this.expirationTime = System.currentTimeMillis() + timeToLive * 1000L;
    }

    /**
     * Return whether this instance has expired depending on the amount of
     * elapsed time since the call to {@link #startExpirationPeriod}.
     */
    public boolean isExpired() {
        return (expirationTime != -1 && System.currentTimeMillis() > expirationTime);
    }

    /**
     * Compare two FlashMaps and prefer the one that specifies a target URL
     * path or has more target URL parameters. Before comparing FlashMap
     * instances ensure that they match a given request.
     */
    @Override
    public int compareTo(@NonNull FlashMap other) {
        int thisUrlPath = (targetRequestName != null ? 1 : 0);
        int otherUrlPath = (other.targetRequestName != null ? 1 : 0);
        return otherUrlPath - thisUrlPath;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof FlashMap that &&
                super.equals(other) &&
                ObjectUtils.nullSafeEquals(targetRequestName, that.targetRequestName)));
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ObjectUtils.nullSafeHashCode(targetRequestName);
        return result;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("targetRequestName", targetRequestName);
        tsb.append("attributes", this);
        return tsb.toString();
    }

}
