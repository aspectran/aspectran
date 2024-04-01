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

    protected volatile long pauseTimeout = -1L;

    DefaultDaemonService() {
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
                Activity.makeFullRequestName(method, name) + "; Cause: " +
                ExceptionUtils.getSimpleMessage(cause), t);
        }
        return translet;
    }

}
