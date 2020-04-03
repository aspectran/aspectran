/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
import com.aspectran.core.component.bean.ContextualBeanRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.ContextualTemplateRenderer;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.support.i18n.message.DelegatingMessageSource;
import com.aspectran.core.support.i18n.message.MessageSource;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

/**
 * The Class AspectranActivityContext.
 * 
 * <p>Created: 2008. 06. 09 PM 2:12:40</p>
 */
public class AspectranActivityContext extends AbstractComponent implements ActivityContext {

    private static final Logger logger = LoggerFactory.getLogger(AspectranActivityContext.class);

    private final ThreadLocal<Activity> currentActivityHolder = new ThreadLocal<>();

    private final ApplicationAdapter applicationAdapter;

    private final ContextEnvironment contextEnvironment;

    private final Activity defaultActivity;

    private DescriptionRule descriptionRule;

    private CoreService rootService;

    private AspectRuleRegistry aspectRuleRegistry;

    private ContextualBeanRegistry contextualBeanRegistry;

    private ContextualTemplateRenderer contextualTemplateRenderer;

    private ScheduleRuleRegistry scheduleRuleRegistry;

    private TransletRuleRegistry transletRuleRegistry;

    private MessageSource messageSource;

    /**
     * Instantiates a new AspectranActivityContext.
     *
     * @param applicationAdapter the application adapter
     * @param contextEnvironment the context environment
     */
    public AspectranActivityContext(ApplicationAdapter applicationAdapter, ContextEnvironment contextEnvironment) {
        this.applicationAdapter = applicationAdapter;
        this.contextEnvironment = contextEnvironment;
        this.defaultActivity = new DefaultActivity(this);
    }

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    @Override
    public String getDescription() {
        if (descriptionRule == null) {
            return null;
        }
        return DescriptionRule.render(descriptionRule, defaultActivity);
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public Environment getEnvironment() {
        return contextEnvironment;
    }

    @Override
    public CoreService getRootService() {
        return rootService;
    }

    @Override
    public void setRootService(CoreService rootService) {
        if (isInitialized()) {
            throw new IllegalStateException("ActivityContext is already specified");
        }
        this.rootService = rootService;
    }

    @Override
    public AspectRuleRegistry getAspectRuleRegistry() {
        return aspectRuleRegistry;
    }

    public void setAspectRuleRegistry(AspectRuleRegistry aspectRuleRegistry) {
        this.aspectRuleRegistry = aspectRuleRegistry;
    }

    @Override
    public BeanRegistry getBeanRegistry() {
        return contextualBeanRegistry;
    }

    /**
     * Sets the context bean registry.
     *
     * @param contextualBeanRegistry the new context bean registry
     */
    public void setContextualBeanRegistry(ContextualBeanRegistry contextualBeanRegistry) {
        this.contextualBeanRegistry = contextualBeanRegistry;
    }

    @Override
    public TemplateRenderer getTemplateRenderer() {
        return contextualTemplateRenderer;
    }

    /**
     * Sets the template processor.
     *
     * @param contextualTemplateRenderer the new template processor
     */
    public void setContextualTemplateRenderer(ContextualTemplateRenderer contextualTemplateRenderer) {
        this.contextualTemplateRenderer = contextualTemplateRenderer;
    }

    @Override
    public ScheduleRuleRegistry getScheduleRuleRegistry() {
        return scheduleRuleRegistry;
    }

    public void setScheduleRuleRegistry(ScheduleRuleRegistry scheduleRuleRegistry) {
        this.scheduleRuleRegistry = scheduleRuleRegistry;
    }

    @Override
    public TransletRuleRegistry getTransletRuleRegistry() {
        return transletRuleRegistry;
    }

    /**
     * Sets the translet rule registry.
     *
     * @param transletRuleRegistry the new translet rule registry
     */
    public void setTransletRuleRegistry(TransletRuleRegistry transletRuleRegistry) {
        this.transletRuleRegistry = transletRuleRegistry;
    }

    @Override
    public MessageSource getMessageSource() {
        if (this.messageSource == null) {
            throw new IllegalStateException("No MessageSource configured");
        }
        return messageSource;
    }

    @Override
    public Activity getDefaultActivity() {
        return defaultActivity;
    }

    @Override
    public Activity getCurrentActivity() {
        Activity activity = currentActivityHolder.get();
        return (activity != null ? activity : getDefaultActivity());
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

    /**
     * Initialize the MessageSource.
     * Use parent's if none defined in this context.
     */
    private void initMessageSource() {
        if (contextualBeanRegistry.containsBean(MessageSource.class, MESSAGE_SOURCE_BEAN_ID)) {
            messageSource = contextualBeanRegistry.getBean(MessageSource.class, MESSAGE_SOURCE_BEAN_ID);
            if (logger.isDebugEnabled()) {
                logger.debug("Using MessageSource [" + messageSource + "]");
            }
        }
        if (messageSource == null) {
            // Use empty MessageSource to be able to accept getMessage calls.
            messageSource = new DelegatingMessageSource();
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_ID +
                        "': using default [" + messageSource + "]");
            }
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        if (aspectRuleRegistry != null) {
            aspectRuleRegistry.initialize();
        }
        if (contextualBeanRegistry != null) {
            contextualBeanRegistry.initialize();
        }
        if (contextualTemplateRenderer != null) {
            contextualTemplateRenderer.initialize();
        }
        if (scheduleRuleRegistry != null) {
            scheduleRuleRegistry.initialize();
        }
        if (transletRuleRegistry != null) {
            transletRuleRegistry.initialize();
        }
        if (contextualBeanRegistry != null) {
            initMessageSource();
        }
    }

    @Override
    protected void doDestroy() {
        if (transletRuleRegistry != null) {
            transletRuleRegistry.destroy();
            transletRuleRegistry = null;
        }
        if (scheduleRuleRegistry != null) {
            scheduleRuleRegistry.destroy();
            scheduleRuleRegistry = null;
        }
        if (contextualTemplateRenderer != null) {
            contextualTemplateRenderer.destroy();
            contextualTemplateRenderer = null;
        }
        if (contextualBeanRegistry != null) {
            contextualBeanRegistry.destroy();
            contextualBeanRegistry = null;
        }
        if (aspectRuleRegistry != null) {
            aspectRuleRegistry.destroy();
            aspectRuleRegistry = null;
        }
    }

}
