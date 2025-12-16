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
package com.aspectran.thymeleaf.context.tow;

import com.aspectran.core.activity.Activity;
import com.aspectran.thymeleaf.context.common.AbstractActivityExchange;
import com.aspectran.thymeleaf.context.common.AspectranWebSession;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;

/**
 * A Thymeleaf {@link IWebExchange} implementation for Aspectran's non-servlet
 * web environment, specifically for the {@link com.aspectran.undertow.activity.TowActivity}.
 *
 * <p>This class extends {@link AbstractActivityExchange} and acts as a container for
 * the suite of non-servlet-specific context objects.</p>
 *
 * <p>Created: 2025-10-07</p>
 */
public class TowActivityExchange extends AbstractActivityExchange {

    private final IWebRequest request;

    private final IWebSession session;

    private final IWebApplication application;

    private TowActivityExchange(
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

    /**
     * Builds a new {@link TowActivityExchange} for the given activity.
     * @param activity the current {@link Activity}
     * @return a new {@code TowActivityExchange} instance
     * @throws IllegalArgumentException if the activity is not a {@link TowActivity}
     */
    @NonNull
    public static TowActivityExchange buildExchange(Activity activity) {
        Assert.notNull(activity, "activity must not be null");
        if (activity instanceof TowActivity towActivity) {
            IWebRequest request = new TowActivityRequest(towActivity.getRequestAdapter());
            IWebSession session = (towActivity.hasSessionAdapter() ? new AspectranWebSession(towActivity.getSessionAdapter()) : null);
            IWebApplication application = new TowActivityApplication(towActivity.getActivityContext());
            return new TowActivityExchange(activity, request, session, application);
        } else {
            throw new IllegalArgumentException("activity must be TowActivity");
        }
    }

}
