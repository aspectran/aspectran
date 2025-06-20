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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.activity.WebActivity;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;

import java.security.Principal;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WebActivityExchange implements IWebExchange {

    private final Activity activity;

    private final WebActivityRequest request;

    private final WebActivitySession session;

    private final WebActivityApplication application;

    public WebActivityExchange(Activity activity,
                               WebActivityRequest request,
                               WebActivitySession session,
                               WebActivityApplication application) {
        this.activity = activity;
        this.request = request;
        this.session = session;
        this.application = application;
    }

    @Override
    public IWebRequest getRequest() {
        return request;
    }

    @Override
    public IWebSession getSession() {
        return session;
    }

    @Override
    public IWebApplication getApplication() {
        return application;
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

    @NonNull
    public static WebActivityExchange buildExchange(Activity activity) {
        Assert.notNull(activity, "activity must not be null");
        if (activity instanceof WebActivity webActivity) {
            WebActivityRequest request = new WebActivityRequest(webActivity.getRequestAdapter());
            WebActivitySession session = (webActivity.hasSessionAdapter() ? new WebActivitySession(webActivity.getSessionAdapter()) : null);
            WebActivityApplication application = new WebActivityApplication(webActivity.getRequest().getServletContext());
            return new WebActivityExchange(activity, request, session, application);
        } else {
            throw new IllegalArgumentException("activity must be WebActivity");
        }
    }

}
