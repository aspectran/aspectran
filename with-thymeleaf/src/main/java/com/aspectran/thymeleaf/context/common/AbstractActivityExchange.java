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
package com.aspectran.thymeleaf.context.common;

import com.aspectran.core.activity.Activity;
import org.thymeleaf.web.IWebExchange;

import java.security.Principal;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link IWebExchange} implementation that is backed by an
 * Aspectran {@link Activity}.
 *
 * <p>Created: 2025-10-08</p>
 */
public abstract class AbstractActivityExchange implements IWebExchange {

    private final Activity activity;

    public AbstractActivityExchange(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Principal getPrincipal() {
        return activity.getRequestAdapter().getPrincipal();
    }

    @Override
    public Locale getLocale() {
        return activity.getRequestAdapter().getLocale();
    }

    @Override
    public String getContentType() {
        return activity.getResponseAdapter().getContentType();
    }

    @Override
    public String getCharacterEncoding() {
        return activity.getResponseAdapter().getEncoding();
    }

    @Override
    public boolean containsAttribute(String name) {
        return activity.getRequestAdapter().hasAttribute(name);
    }

    @Override
    public int getAttributeCount() {
        return activity.getRequestAdapter().getAttributeMap().size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return Collections.unmodifiableSet(activity.getRequestAdapter().getAttributeNames());
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return Collections.unmodifiableMap(activity.getRequestAdapter().getAttributeMap());
    }

    @Override
    public Object getAttributeValue(String name) {
        return activity.getRequestAdapter().getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        activity.getRequestAdapter().setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        activity.getRequestAdapter().removeAttribute(name);
    }

    @Override
    public String transformURL(String url) {
        return activity.getResponseAdapter().transformPath(url);
    }

}
