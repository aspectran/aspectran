/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

public abstract class AbstractViewDispatcher implements ViewDispatcher {

    private String contentType;

    private String prefix;

    private String suffix;

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix for the template name.
     * @param prefix the new prefix for the template name
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    protected String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix for the template name.
     * @param suffix the new suffix for the template name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

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
