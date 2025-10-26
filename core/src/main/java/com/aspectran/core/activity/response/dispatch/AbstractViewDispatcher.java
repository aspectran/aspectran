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
 * <p>This class provides common functionality for view dispatchers, such as managing
 * the content type, prefix, and suffix for view names. Subclasses are expected to
 * implement the {@link #dispatch(Activity, DispatchRule)} method to integrate with a
 * specific view technology. The {@link #resolveViewName} method is provided as a
 * utility to construct the final view name from a {@link DispatchRule}.</p>
 */
public abstract class AbstractViewDispatcher implements ViewDispatcher {

    private String contentType;

    private String prefix;

    private String suffix;

    /**
     * Returns the content type to be set in the response.
     * @return the content type
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type to be set in the response.
     * @param contentType the content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the prefix that is prepended to view names.
     * @return the prefix for view names
     */
    protected String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix that is prepended to view names.
     * @param prefix the prefix for view names
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the suffix that is appended to view names.
     * @return the suffix for view names
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix that is appended to view names.
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
     * Resolves the final view name by applying the configured prefix and suffix to the
     * name specified in the {@code DispatchRule}. For example, if the prefix is
     * "/WEB-INF/views/", the suffix is ".jsp", and the view name from the rule is "home",
     * the resolved name will be "/WEB-INF/views/home.jsp".
     * @param dispatchRule the rule that defines the view name
     * @param activity the current activity, used for dynamic name resolution if the
     *      name contains parameter tokens
     * @return the resolved view name, ready for dispatching
     * @throws IllegalArgumentException if the dispatch rule does not have a name or
     *      the view name cannot be resolved
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
