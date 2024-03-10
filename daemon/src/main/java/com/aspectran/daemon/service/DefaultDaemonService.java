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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.daemon.activity.DaemonActivity;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.Map;

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
    public Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap) {
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
        return translate(name, requestMethod, attributeMap, parameterMap);
    }

    @Override
    public Translet translate(String name, MethodType method,
                              Map<String, Object> attributeMap, ParameterMap parameterMap) {
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

        DaemonActivity activity = null;
        Translet translet = null;
        try {
            activity = new DaemonActivity(this);
            activity.setAttributeMap(attributeMap);
            activity.setParameterMap(parameterMap);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            Throwable t;
            if (activity != null && activity.getRaisedException() != null) {
                t = activity.getRaisedException();
            } else {
                t = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(t);
            throw new AspectranServiceException("Error occurred while processing request: " +
                Activity.makeRequestName(method, name) + "; Cause: " +
                ExceptionUtils.getSimpleMessage(cause), t);
        }
        return translet;
    }

    /**
     * Returns a new instance of {@code DefaultDaemonService}.
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code DefaultDaemonService}
     */
    @NonNull
    public static DefaultDaemonService create(@NonNull AspectranConfig aspectranConfig) {
        DefaultDaemonService daemonService = new DefaultDaemonService();
        daemonService.configure(aspectranConfig);
        DaemonConfig daemonConfig = aspectranConfig.getDaemonConfig();
        if (daemonConfig != null) {
            applyDaemonConfig(daemonService, daemonConfig);
        }
        setServiceStateListener(daemonService);
        return daemonService;
    }

    private static void applyDaemonConfig(@NonNull DefaultDaemonService daemonService,
                                          @NonNull DaemonConfig daemonConfig) {
        ExposalsConfig exposalsConfig = daemonConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            daemonService.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(@NonNull final DefaultDaemonService daemonService) {
        daemonService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(daemonService);
                daemonService.createSessionManager();
                daemonService.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    daemonService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                daemonService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                started();
            }

            @Override
            public void stopped() {
                paused();
                daemonService.destroySessionManager();
                CoreServiceHolder.release(daemonService);
            }
        });
    }

}
