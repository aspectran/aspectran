/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.ObjectUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.daemon.activity.DaemonActivity;

import java.util.Map;

import static com.aspectran.core.context.config.AspectranConfig.DEFAULT_APP_CONTEXT_FILE;

/**
 * The Class DefaultDaemonService.
 *
 * @since 5.1.0
 */
public class DefaultDaemonService extends AbstractDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDaemonService.class);

    private volatile long pauseTimeout = -1L;

    public DefaultDaemonService() {
        super();
    }

    @Override
    public Translet translate(String name, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        MethodType requestMethod = null;
        for (MethodType methodType : MethodType.values()) {
            if (name.startsWith(methodType.name() + " ")) {
                requestMethod = methodType;
                name = name.substring(methodType.name().length()).trim();
            }
        }
        return translate(name, requestMethod, parameterMap, attributeMap);
    }

    @Override
    public Translet translate(String name, MethodType method,
                              ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (!isExposable(name)) {
            logger.error("Unavailable translet: " + name);
            return null;
        }
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " is paused, so did not execute translet: " + name);
                }
                return null;
            } else {
                pauseTimeout = 0L;
            }
        }

        Translet translet = null;
        try {
            DaemonActivity activity = new DaemonActivity(this);
            activity.setParameterMap(parameterMap);
            activity.setAttributeMap(attributeMap);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new AspectranServiceException("An error occurred while processing translet: " + name, e);
        }
        return translet;
    }

    /**
     * Returns a new instance of {@code DefaultDaemonService}.
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code DefaultDaemonService}
     */
    public static DefaultDaemonService create(AspectranConfig aspectranConfig) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String[] contextRules = contextConfig.getContextRules();
        if (ObjectUtils.isEmpty(contextRules) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setContextRules(new String[] {DEFAULT_APP_CONTEXT_FILE});
        }

        DefaultDaemonService service = new DefaultDaemonService();
        service.prepare(aspectranConfig);
        DaemonConfig daemonConfig = aspectranConfig.getDaemonConfig();
        if (daemonConfig != null) {
            applyDaemonConfig(service, daemonConfig);
        }
        setServiceStateListener(service);
        return service;
    }

    private static void applyDaemonConfig(DefaultDaemonService service, DaemonConfig daemonConfig) {
        ExposalsConfig exposalsConfig = daemonConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final DefaultDaemonService service) {
        service.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                service.initSessionManager();
                service.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                service.destroySessionManager();
                service.initSessionManager();
                service.pauseTimeout = 0L;
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    service.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                service.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                started();
            }

            @Override
            public void stopped() {
                paused();
                service.destroySessionManager();
            }
        });
    }

}
