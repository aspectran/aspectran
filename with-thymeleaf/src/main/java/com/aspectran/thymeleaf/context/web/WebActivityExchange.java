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
import com.aspectran.thymeleaf.context.common.AbstractActivityExchange;
import com.aspectran.thymeleaf.context.common.AspectranWebSession;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.web.adapter.WebRequestAdapter;
import org.jspecify.annotations.NonNull;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;

/**
 * A Thymeleaf {@link IWebExchange} implementation for Aspectran's web environment.
 *
 * <p>This class extends {@link AbstractActivityExchange} and provides access to
 * the request, session, and application objects, adapted for Thymeleaf's web context.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityExchange extends AbstractActivityExchange {

    private final IWebRequest request;

    private final IWebSession session;

    private final IWebApplication application;

    /**
     * Instantiates a new WebActivityExchange.
     * @param activity the activity
     * @param request the request
     * @param session the session
     * @param application the application
     */
    private WebActivityExchange(
            Activity activity,
            IWebRequest request,
            IWebSession session,
            IWebApplication application) {
        super(activity);
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

    private static final boolean SERVLET_API_PRESENT =
            ClassUtils.isPresent("jakarta.servlet.http.HttpServletRequest", WebActivityExchange.class.getClassLoader());

    /**
     * Builds a new {@link WebActivityExchange} for the given activity.
     * @param activity the current {@link Activity}
     * @return a new {@code WebActivityExchange} instance
     * @throws IllegalArgumentException if the activity does not have a {@link WebRequestAdapter}
     */
    @NonNull
    public static WebActivityExchange buildExchange(Activity activity) {
        Assert.notNull(activity, "activity must not be null");
        if (activity.getRequestAdapter() instanceof WebRequestAdapter webRequestAdapter) {
            IWebRequest request = new WebActivityRequest(webRequestAdapter);
            IWebSession session = (activity.hasSessionAdapter() ? new AspectranWebSession(activity.getSessionAdapter()) : null);
            IWebApplication application = createWebApplication(activity);
            return new WebActivityExchange(activity, request, session, application);
        } else {
            throw new IllegalArgumentException("Activity must have a WebRequestAdapter");
        }
    }

    @NonNull
    private static IWebApplication createWebApplication(Activity activity) {
        if (SERVLET_API_PRESENT && activity.getRequestAdapter() != null) {
            Object adaptee = activity.getRequestAdapter().getAdaptee();
            if (ServletWebExchangeHelper.isHttpServletRequest(adaptee)) {
                return ServletWebExchangeHelper.createApplication(adaptee);
            }
        }
        return new WebActivityApplication(activity.getActivityContext());
    }

}

