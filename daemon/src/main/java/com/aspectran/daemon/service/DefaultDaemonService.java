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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.daemon.activity.DaemonActivity;
import com.aspectran.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        MethodType requestMethod = null;
        if (name != null) {
            for (MethodType methodType : MethodType.values()) {
                if (name.startsWith(methodType.name() + " ")) {
                    requestMethod = methodType;
                    name = name.substring(methodType.name().length()).trim();
                }
            }
        }
        return translate(name, requestMethod, attributeMap, parameterMap);
    }

    @Override
    public Translet translate(String name, MethodType method,
                              Map<String, Object> attributeMap, ParameterMap parameterMap) {
        if (checkPaused(name)) {
            return null;
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (!isRequestAcceptable(name)) {
            logger.error("Unavailable translet: {}", name);
            return null;
        }

        DaemonActivity activity = new DaemonActivity(this);
        activity.setRequestName(name);
        activity.setRequestMethod(method != null ? method : MethodType.GET);
        activity.setAttributeMap(attributeMap);
        activity.setParameterMap(parameterMap);
        Translet translet = null;
        try {
            activity.prepare();
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: {}", e.getMessage());
            }
        } catch (Exception e) {
            Throwable t;
            if (activity.getRaisedException() != null) {
                t = activity.getRaisedException();
            } else {
                t = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(t);
            throw new CoreServiceException("Error occurred while processing request: " +
                activity.getFullRequestName() + "; Cause: " + ExceptionUtils.getSimpleMessage(cause), t);
        }
        return translet;
    }

    private boolean checkPaused(String name) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is paused, so did not execute translet: {}", getServiceName(), name);
                }
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }
        return false;
    }

}
