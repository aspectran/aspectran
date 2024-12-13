/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.scheduler.service.DefaultSchedulerServiceBuilder;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * The Class AbstractCoreService.
 */
public abstract class AbstractCoreService extends AbstractServiceLifeCycle implements CoreService {

    private final boolean derived;

    private String basePath;

    private AspectranConfig aspectranConfig;

    private ActivityContextBuilder activityContextBuilder;

    private ActivityContext activityContext;

    private ClassLoader serviceClassLoader;

    private ClassLoader altClassLoader;

    private SchedulerService schedulerService;

    private RequestAcceptor requestAcceptor;

    public AbstractCoreService() {
        this(null, false);
    }

    public AbstractCoreService(CoreService parentService, boolean derived) {
        super(parentService);

        if (parentService == null) {
            Assert.isTrue(!derived, "When in derived mode, parentService must not be null");
        } else if (derived) {
            Assert.state(parentService.getActivityContext() != null,
                    "Oops! No ActivityContext configured");
        }

        this.derived = derived;
        if (parentService != null && derived) {
            setAspectranConfig(parentService.getAspectranConfig());
            setActivityContext(parentService.getActivityContext());
        }
    }

    @Override
    public boolean isDerived() {
        return derived;
    }

    @Override
    public String getServiceName() {
        if (getActivityContext() != null && getActivityContext().getName() != null) {
            return ObjectUtils.simpleIdentityToString(this, getActivityContext().getName());
        } else {
            return super.getServiceName();
        }
    }

    @Override
    public ServiceLifeCycle getServiceLifeCycle() {
        return this;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    protected void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    protected boolean hasActivityContextBuilder() {
        return (activityContextBuilder != null);
    }

    protected ActivityContextBuilder getActivityContextBuilder() {
        Assert.state(hasActivityContextBuilder(),
            "No ActivityContextLoader configured");
        return activityContextBuilder;
    }

    protected void setActivityContextBuilder(ActivityContextBuilder activityContextBuilder) {
        Assert.state(!hasActivityContextBuilder(),
            "ActivityContextBuilder is already configured");
        this.activityContextBuilder = activityContextBuilder;
    }

    @Nullable
    public ApplicationAdapter getApplicationAdapter() {
        if (getRootService().getActivityContext() != null) {
            return getRootService().getActivityContext().getApplicationAdapter();
        } else {
            return null;
        }
    }

    @Override
    public ActivityContext getActivityContext() {
        return activityContext;
    }

    protected void setActivityContext(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public Activity getDefaultActivity() {
        Assert.state(getActivityContext() != null, "No ActivityContext configured yet");
        return getActivityContext().getDefaultActivity();
    }

    @Override
    public boolean hasServiceClassLoader() {
        return (serviceClassLoader != null);
    }

    @Override
    @Nullable
    public ClassLoader getServiceClassLoader() {
        if (serviceClassLoader != null) {
            return serviceClassLoader;
        } else if (activityContext != null) {
            return activityContext.getClassLoader();
        } else if (getActivityContextBuilder() != null) {
            return getActivityContextBuilder().getClassLoader();
        } else {
            return null;
        }
    }

    protected void setServiceClassLoader(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    @Override
    public ClassLoader getAltClassLoader() {
        return altClassLoader;
    }

    public void setAltClassLoader(ClassLoader altClassLoader) {
        this.altClassLoader = altClassLoader;
    }

    @Override
    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    protected void buildSchedulerService() {
        Assert.state(getAspectranConfig() != null, "AspectranConfig is not set");
        if (getAspectranConfig().hasSchedulerConfig() && getAspectranConfig().getSchedulerConfig().isEnabled()) {
            this.schedulerService = DefaultSchedulerServiceBuilder.build(this, getAspectranConfig().getSchedulerConfig());
        }
    }

    @Override
    public boolean isAcceptable(String requestName) {
        return (requestAcceptor == null || requestAcceptor.isAcceptable(requestName));
    }

    protected void setRequestAcceptor(RequestAcceptor requestAcceptor) {
        this.requestAcceptor = requestAcceptor;
    }

}
