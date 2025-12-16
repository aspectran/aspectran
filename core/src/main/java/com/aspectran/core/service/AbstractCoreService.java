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
package com.aspectran.core.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.FlashMapManager;
import com.aspectran.core.activity.support.SessionFlashMapManager;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.NoSuchBeanException;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.scheduler.service.DefaultSchedulerServiceBuilder;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.core.support.i18n.locale.LocaleResolver;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link CoreService} implementations.
 *
 * <p>This class provides a skeletal implementation of the {@link CoreService} interface,
 * handling common functionalities such as managing the {@link ActivityContext},
 * {@link AspectranConfig}, and {@link SchedulerService}. It introduces the concept
 * of a "derived" service, which can inherit the {@code ActivityContext} from a
 * parent service. This is useful in environments where multiple service endpoints
 * need to share a common application context.
 *
 * <p>Key responsibilities of this class include:
 * <ul>
 *     <li>Managing the lifecycle of the service (see {@link AbstractServiceLifeCycle}).</li>
 *     <li>Holding references to the core components like {@link ActivityContext} and
 *     {@link AspectranConfig}.</li>
 *     <li>Providing mechanisms for building and destroying the {@link SchedulerService}.</li>
 *     <li>Handling class loaders for the service.</li>
 *     <li>Initializing and providing access to shared components like {@link FlashMapManager}
 *     and {@link LocaleResolver}.</li>
 * </ul>
 *
 * <p>Concrete implementations should extend this class and provide specific strategies
 * for building and managing the {@code ActivityContext}.
 *
 * @see com.aspectran.core.service.CoreService
 * @see com.aspectran.core.service.DefaultCoreService
 */
public abstract class AbstractCoreService extends AbstractServiceLifeCycle implements CoreService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCoreService.class);

    private final boolean derived;

    private String basePath;

    private String contextName;

    private AspectranConfig aspectranConfig;

    private ActivityContextBuilder activityContextBuilder;

    private ActivityContext activityContext;

    private ClassLoader serviceClassLoader;

    private ClassLoader altClassLoader;

    private SchedulerService schedulerService;

    private FlashMapManager flashMapManager;

    private LocaleResolver localeResolver;

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

    /**
     * Sets the base path for the root application.
     * @param basePath the base path for the root application
     */
    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getContextName() {
        return contextName;
    }

    /**
     * Sets the name of this service's context.
     * @param contextName the context name
     */
    protected void setContextName(String contextName) {
        this.contextName = contextName;
    }

    @Override
    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    /**
     * Sets the Aspectran configuration parameters.
     * @param aspectranConfig the Aspectran configuration parameters
     */
    protected void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    /**
     * Returns whether an {@code ActivityContextBuilder} is configured.
     * @return true if an {@code ActivityContextBuilder} is configured, false otherwise
     */
    protected boolean hasActivityContextBuilder() {
        return (activityContextBuilder != null);
    }

    /**
     * Returns the {@code ActivityContextBuilder}.
     * @return the {@code ActivityContextBuilder}
     * @throws IllegalStateException if the {@code ActivityContextBuilder} is not configured
     */
    protected ActivityContextBuilder getActivityContextBuilder() {
        Assert.state(hasActivityContextBuilder(), "No ActivityContextLoader configured");
        return activityContextBuilder;
    }

    /**
     * Sets the {@code ActivityContextBuilder}.
     * @param activityContextBuilder the {@code ActivityContextBuilder}
     * @throws IllegalStateException if the {@code ActivityContextBuilder} is already configured
     */
    protected void setActivityContextBuilder(ActivityContextBuilder activityContextBuilder) {
        Assert.state(!hasActivityContextBuilder(), "ActivityContextBuilder is already configured");
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

    /**
     * Sets the {@code ActivityContext}.
     * @param activityContext the {@code ActivityContext}
     */
    protected void setActivityContext(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    /**
     * Checks if the {@code ActivityContext} is configured.
     * @throws IllegalStateException if the {@code ActivityContext} is not configured
     */
    protected void checkContextConfigured() {
        if (activityContext == null) {
            throw new IllegalStateException("No ActivityContext configured yet");
        }
    }

    @Override
    public Activity getDefaultActivity() {
        checkContextConfigured();
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
            return getActivityContextBuilder().getSiblingClassLoader();
        } else {
            return null;
        }
    }

    /**
     * Sets the service-specific class loader.
     * @param serviceClassLoader the service-specific class loader
     */
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

    /**
     * Builds the scheduler service based on the current configuration.
     */
    protected void buildSchedulerService() {
        Assert.state(getAspectranConfig() != null, "AspectranConfig is not set");
        SchedulerConfig schedulerConfig = getAspectranConfig().getSchedulerConfig();
        if (schedulerConfig != null && schedulerConfig.isEnabled()) {
            schedulerService = DefaultSchedulerServiceBuilder.build(this, schedulerConfig);
        }
    }

    /**
     * Destroys the scheduler service.
     */
    protected void destroySchedulerService() {
        if (schedulerService != null) {
            if (schedulerService.isActive()) {
                schedulerService.stop();
            }
            schedulerService.withdraw();
            schedulerService = null;
        }
    }

    @Override
    public boolean isRequestAcceptable(String requestName) {
        return (requestAcceptor == null || requestAcceptor.isAcceptable(requestName));
    }

    /**
     * Sets the request acceptor.
     * @param requestAcceptor the request acceptor
     */
    protected void setRequestAcceptor(RequestAcceptor requestAcceptor) {
        this.requestAcceptor = requestAcceptor;
    }

    @Override
    public FlashMapManager getFlashMapManager() {
        return flashMapManager;
    }

    /**
     * Initializes the {@code FlashMapManager}.
     * If this service is a derived service, it uses the parent's {@code FlashMapManager}.
     * Otherwise, it tries to get a bean from the {@code ActivityContext},
     * falling back to a {@code SessionFlashMapManager} if none is found.
     */
    protected void initFlashMapManager() {
        if (isDerived()) {
            flashMapManager = getParentService().getFlashMapManager();
        } else {
            checkContextConfigured();
            try {
                flashMapManager = getActivityContext().getBeanRegistry().getBean(FlashMapManager.class);
                if (logger.isTraceEnabled()) {
                    logger.trace("Detected {}", flashMapManager);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Detected {}", flashMapManager.getClass().getSimpleName());
                }
            } catch (NoUniqueBeanException e) {
                flashMapManager = getActivityContext().getBeanRegistry().getBean(FlashMapManager.class,
                        FlashMapManager.FLASH_MAP_MANAGER_BEAN_ID);
                if (logger.isTraceEnabled()) {
                    logger.trace("Detected {}", flashMapManager);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Detected {}", flashMapManager.getClass().getSimpleName());
                }
            } catch (NoSuchBeanException e) {
                flashMapManager = new SessionFlashMapManager();
                if (logger.isTraceEnabled()) {
                    logger.trace("No FlashMapManager: using default [{}]", flashMapManager);
                }
            }
        }
    }

    @Override
    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    /**
     * Sets the {@code LocaleResolver}.
     * @param localeResolver the locale resolver
     */
    protected void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    /**
     * Initializes the {@code LocaleResolver}.
     * If this service is a derived service, it uses the parent's {@code LocaleResolver}.
     * Otherwise, it tries to get a bean from the {@code ActivityContext}.
     */
    protected void initLocaleResolver() {
        if (isDerived()) {
            localeResolver = getParentService().getLocaleResolver();
        } else {
            checkContextConfigured();
            try {
                localeResolver = getActivityContext().getBeanRegistry().getBean(LocaleResolver.class);
                if (logger.isTraceEnabled()) {
                    logger.trace("Detected {}", localeResolver);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Detected {}", localeResolver.getClass().getSimpleName());
                }
            } catch (NoUniqueBeanException e) {
                localeResolver = getActivityContext().getBeanRegistry().getBean(LocaleResolver.class,
                        LocaleResolver.LOCALE_RESOLVER_BEAN_ID);
                if (logger.isTraceEnabled()) {
                    logger.trace("Detected {}", localeResolver);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Detected {}", localeResolver.getClass().getSimpleName());
                }
            } catch (NoSuchBeanException e) {
                // ignore
            }
        }
    }

}
