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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Abstract base class for {@link ViewDispatcher} implementations.
 *
 * <p>Provides common functionality for view dispatchers, such as managing content type,
 * prefixes, and suffixes for view names. It also includes a utility method to resolve
 * the final view name based on the {@link DispatchRule} and the current {@link Activity}.
 * Implementations typically extend this class to integrate with specific view technologies.</p>
 */
public abstract class AbstractViewDispatcher implements ViewDispatcher {

    private String contentType;

    private String prefix;

    private String suffix;

    /**
     * Returns the content type of the view being dispatched.
     * @return the content type
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type for the view being dispatched.
     * @param contentType the content type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the prefix that is prepended to view names when building a URL.
     * @return the prefix for view names
     */
    protected String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix that is prepended to view names when building a URL.
     * @param prefix the prefix for view names
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the suffix that is appended to view names when building a URL.
     * @return the suffix for view names
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix that is appended to view names when building a URL.
     * @param suffix the suffix for view names
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Resolves the final view name by applying configured prefixes and suffixes to the
     * name specified in the {@code DispatchRule}.
     * @param dispatchRule the rule that defines the view name
     * @param activity the current activity, used for dynamic name resolution
     * @return the resolved view name, ready for dispatching
     * @throws IllegalArgumentException if the dispatch rule does not have a name or the view name cannot be resolved
     */
    @NonNull
    protected String resolveViewName(@NonNull DispatchRule dispatchRule, Activity activity) {
        if (dispatchRule.getName() == null) {
            throw new IllegalArgumentException("Dispatch rule must have a name");
        }
        String viewName = dispatchRule.getName(activity);
        if (viewName == null) {
            throw new IllegalArgumentException("Cannot resolve view name");
        }
        if (prefix != null && suffix != null) {
            viewName = prefix + viewName + suffix;
        } else if (prefix != null) {
            viewName = prefix + viewName;
        } else if (suffix != null) {
            viewName = viewName + suffix;
        }
        return viewName;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", super.toString());
        tsb.append("defaultContentType", contentType);
        tsb.append("prefix", prefix);
        tsb.append("suffix", suffix);
        return tsb.toString();
    }

}
