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
package com.aspectran.core.context;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.DefaultActivity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.component.bean.DefaultBeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.DefaultTemplateRenderer;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.ActivityEnvironment;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.support.i18n.message.DelegatingMessageSource;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.utils.Assert;
import com.aspectran.utils.statistic.CounterStatistic;
import com.aspectran.utils.thread.ThreadContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DefaultActivityContext.
 *
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public class DefaultActivityContext extends AbstractComponent implements ActivityContext {

    private static final Logger logger = LoggerFactory.getLogger(DefaultActivityContext.class);

    private final ThreadLocal<Activity> currentActivityHolder = new ThreadLocal<>();

    private final CounterStatistic activityCounter = new CounterStatistic();

    private final ClassLoader classLoader;

    private final ApplicationAdapter applicationAdapter;

    private final CoreService masterService;

    private final Activity defaultActivity;

    private String name;

    private DescriptionRule descriptionRule;

    private ActivityEnvironment activityEnvironment;

    private DefaultBeanRegistry beanRegistry;

    private DefaultTemplateRenderer templateRenderer;

    private AspectRuleRegistry aspectRuleRegistry;

    private ScheduleRuleRegistry scheduleRuleRegistry;

    private TransletRuleRegistry transletRuleRegistry;

    private MessageSource messageSource;

    /**
     * Instantiates a new DefaultActivityContext.
     * @param classLoader the class loader
     * @param applicationAdapter the application adapter
     */
    public DefaultActivityContext(ClassLoader classLoader, ApplicationAdapter applicationAdapter, CoreService masterService) {
        this.classLoader = classLoader;
        this.applicationAdapter = applicationAdapter;
        this.masterService = masterService;
        this.defaultActivity = new DefaultActivity(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkInitializable();
        this.name = name;
    }

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        checkInitializable();
        this.descriptionRule = descriptionRule;
    }

    @Override
    public String getDescription() {
        if (descriptionRule != null) {
            return DescriptionRule.render(descriptionRule, defaultActivity);
        } else {
            return null;
        }
    }

    @Override
    public CoreService getMasterService() {
        return masterService;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public Environment getEnvironment() {
        checkNotDestroyed();
        return activityEnvironment;
    }

    public void setEnvironment(ActivityEnvironment activityEnvironment) {
        checkInitializable();
        this.activityEnvironment = activityEnvironment;
    }

    @Override
    public BeanRegistry getBeanRegistry() {
        checkNotDestroyed();
        return beanRegistry;
    }

    /**
     * Sets the default bean registry.
     * @param beanRegistry the new default bean registry
     */
    public void setBeanRegistry(DefaultBeanRegistry beanRegistry) {
        checkInitializable();
        this.beanRegistry = beanRegistry;
    }

    @Override
    public TemplateRenderer getTemplateRenderer() {
        checkNotDestroyed();
        return templateRenderer;
    }

    /**
     * Sets the template processor.
     * @param templateRenderer the new template processor
     */
    public void setTemplateRenderer(DefaultTemplateRenderer templateRenderer) {
        checkInitializable();
        this.templateRenderer = templateRenderer;
    }

    @Override
    public AspectRuleRegistry getAspectRuleRegistry() {
        checkNotDestroyed();
        return aspectRuleRegistry;
    }

    public void setAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry) {
        checkInitializable();
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    @Override
    public ScheduleRuleRegistry getScheduleRuleRegistry() {
        checkNotDestroyed();
        return scheduleRuleRegistry;
    }

    public void setScheduleRuleRegistry(ScheduleRuleRegistry scheduleRuleRegistry) {
        checkInitializable();
        this.scheduleRuleRegistry = scheduleRuleRegistry;
    }

    @Override
    public TransletRuleRegistry getTransletRuleRegistry() {
        checkNotDestroyed();
        return transletRuleRegistry;
    }

    /**
     * Sets the translet rule registry.
     * @param transletRuleRegistry the new translet rule registry
     */
    public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
        checkInitializable();
        this.transletRuleRegistry = transletRuleRegistry;
    }

    @Override
    public MessageSource getMessageSource() {
        checkNotDestroyed();
        if (messageSource == null) {
            resolveMessageSource();
        }
        return messageSource;
    }

    @Override
    public Activity getDefaultActivity() {
        return defaultActivity;
    }

    @Override
    public Activity getAvailableActivity() {
        Activity activity = currentActivityHolder.get();
        return (activity != null ? activity : getDefaultActivity());
    }

    @Override
    public Activity getCurrentActivity() {
        Activity activity = currentActivityHolder.get();
        if (activity == null) {
            throw new NoActivityStateException();
        }
        return activity;
    }

    @Override
    public void setCurrentActivity(Activity activity) {
        currentActivityHolder.set(activity);
    }

    @Override
    public void removeCurrentActivity() {
        currentActivityHolder.remove();
    }

    @Override
    public boolean hasCurrentActivity() {
        return (currentActivityHolder.get() != null);
    }

    @Override
    public CounterStatistic getActivityCounter() {
        return activityCounter;
    }

    /**
     * Initialize the MessageSource.
     * Use parent's if none defined in this context.
     */
    private void resolveMessageSource() {
        if (beanRegistry.containsBean(MessageSource.class, MESSAGE_SOURCE_BEAN_ID)) {
            messageSource = beanRegistry.getBean(MessageSource.class, MESSAGE_SOURCE_BEAN_ID);
            if (logger.isDebugEnabled()) {
                logger.debug("Using MessageSource [{}]", messageSource);
            }
        }
        if (messageSource == null) {
            // Use empty MessageSource to be able to accept getMessage calls.
            messageSource = new DelegatingMessageSource();
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate MessageSource with name '{}': using default [{}]",
                        MESSAGE_SOURCE_BEAN_ID,
                        messageSource);
            }
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        Assert.state(beanRegistry != null, "BeanRegistry is not set");
        Assert.state(aspectRuleRegistry != null, "AspectRuleRegistry is not set");
        Assert.state(scheduleRuleRegistry != null, "ScheduleRuleRegistry is not set");
        Assert.state(transletRuleRegistry != null, "TransletRuleRegistry is not set");
        Assert.state(templateRenderer != null, "TemplateRenderer is not set");
        ThreadContextHelper.runThrowable(getClassLoader(), () -> {
            beanRegistry.initialize();
            aspectRuleRegistry.initialize();
            scheduleRuleRegistry.initialize();
            transletRuleRegistry.initialize();
            templateRenderer.initialize();
            resolveMessageSource();
        });
    }

    @Override
    protected void doDestroy() {
        ThreadContextHelper.run(getClassLoader(), () -> {
            if (beanRegistry != null) {
                beanRegistry.destroy();
                beanRegistry = null;
            }
            if (templateRenderer != null) {
                templateRenderer.destroy();
                templateRenderer = null;
            }
            if (transletRuleRegistry != null) {
                transletRuleRegistry.destroy();
                transletRuleRegistry = null;
            }
            if (scheduleRuleRegistry != null) {
                scheduleRuleRegistry.destroy();
                scheduleRuleRegistry = null;
            }
            if (aspectRuleRegistry != null) {
                aspectRuleRegistry.destroy();
                aspectRuleRegistry = null;
            }
        });
    }

}
