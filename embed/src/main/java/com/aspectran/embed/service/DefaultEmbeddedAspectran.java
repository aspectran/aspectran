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
package com.aspectran.embed.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.InstantAction;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantActivityException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.embed.activity.AspectranActivity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.Map;

/**
 * Provides an interface that can be used by embedding Aspectran in Java applications.
 *
 * @since 3.0.0
 */
public class DefaultEmbeddedAspectran extends AbstractEmbeddedAspectran {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEmbeddedAspectran.class);

    private volatile long pauseTimeout = -1L;

    public DefaultEmbeddedAspectran() {
        super();
    }

    @Override
    public <V> V execute(InstantAction<V> instantAction) {
        try {
            InstantActivity activity = new InstantActivity(getActivityContext());
            return activity.perform(instantAction);
        } catch (Exception e) {
            throw new InstantActivityException(e);
        }
    }

    @Override
    public Translet translate(String name) {
        return translate(name, null, null, null, null);
    }

    @Override
    public Translet translate(String name, String body) {
        return translate(name, null, null, null, body);
    }

    @Override
    public Translet translate(String name, Map<String, Object> attributeMap) {
        return translate(name, null, attributeMap, null, null);
    }

    @Override
    public Translet translate(String name, ParameterMap parameterMap) {
        return translate(name, null, null, parameterMap, null);
    }

    @Override
    public Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap) {
        return translate(name, null, attributeMap, parameterMap, null);
    }

    @Override
    public Translet translate(String name, MethodType method) {
        return translate(name, method, null, null, null);
    }

    @Override
    public Translet translate(String name, MethodType method, Map<String, Object> attributeMap) {
        return translate(name, method, attributeMap, null, null);
    }

    @Override
    public Translet translate(String name, MethodType method, ParameterMap parameterMap) {
        return translate(name, method, null, parameterMap, null);
    }

    @Override
    public Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap) {
        return translate(name, method, attributeMap, parameterMap, null);
    }

    @Override
    public Translet translate(String name, @Nullable MethodType method,
                              @Nullable Map<String, Object> attributeMap, @Nullable ParameterMap parameterMap,
                              @Nullable String body) {
        Assert.notNull(name, "name must not be null");

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

        AspectranActivity activity = null;
        Translet translet = null;
        try {
            activity = new AspectranActivity(this);
            activity.setAttributeMap(attributeMap);
            activity.setParameterMap(parameterMap);
            activity.setBody(body);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            Throwable throwable;
            if (activity != null && activity.getRaisedException() != null) {
                throwable = activity.getRaisedException();
            } else {
                throwable = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(throwable);
            throw new AspectranServiceException("Error occurred while processing request: " +
                Activity.makeRequestName(method, name) + "; Cause: " +
                ExceptionUtils.getSimpleMessage(cause), throwable);
        }
        return translet;
    }

    @Override
    public String render(String templateId) {
        return render(templateId, null, null);
    }

    @Override
    public String render(String templateId, Map<String, Object> attributeMap) {
        return render(templateId, attributeMap, null);
    }

    @Override
    public String render(String templateId, ParameterMap parameterMap) {
        return render(templateId, null, parameterMap);
    }

    @Override
    public String render(String templateId, Map<String, Object> attributeMap, ParameterMap parameterMap) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " is paused, so did not execute template: " + templateId);
                }
                return null;
            } else {
                pauseTimeout = 0L;
            }
        }

        try {
            InstantActivity activity = new InstantActivity(getActivityContext());
            activity.setSessionAdapter(newSessionAdapter());
            activity.setAttributeMap(attributeMap);
            activity.setParameterMap(parameterMap);
            Object result = activity.perform(() -> getActivityContext().getTemplateRenderer().render(templateId));
            return result.toString();
        } catch (Exception e) {
            throw new AspectranServiceException("Error while rendering template: " + templateId, e);
        }
    }

    /**
     * Returns a new instance of {@code DefaultEmbeddedAspectran}.
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code DefaultEmbeddedAspectran}
     */
    @NonNull
    static DefaultEmbeddedAspectran create(AspectranConfig aspectranConfig) {
        DefaultEmbeddedAspectran aspectran = new DefaultEmbeddedAspectran();
        aspectran.configure(aspectranConfig);
        setServiceStateListener(aspectran);
        return aspectran;
    }

    private static void setServiceStateListener(@NonNull final DefaultEmbeddedAspectran aspectran) {
        aspectran.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                aspectran.createSessionManager();
                aspectran.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    aspectran.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                aspectran.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                started();
            }

            @Override
            public void stopped() {
                aspectran.destroySessionManager();
                paused();
            }
        });
    }

}
