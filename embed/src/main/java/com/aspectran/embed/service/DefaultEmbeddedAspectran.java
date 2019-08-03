/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.embed.activity.AspectranActivity;

import java.util.Map;

import static com.aspectran.core.context.config.AspectranConfig.DEFAULT_APP_CONFIG_ROOT_FILE;

/**
 * Provides an interface that can be used by embedding Aspectran in Java applications.
 *
 * @since 3.0.0
 */
public class DefaultEmbeddedAspectran extends AbstractEmbeddedAspectran {

    private static final Log log = LogFactory.getLog(DefaultEmbeddedAspectran.class);

    private volatile long pauseTimeout = -1L;

    public DefaultEmbeddedAspectran() {
        super();
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
    public Translet translate(String name, ParameterMap parameterMap) {
        return translate(name, null, parameterMap, null, null);
    }

    @Override
    public Translet translate(String name, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        return translate(name, null, parameterMap, attributeMap, null);
    }

    @Override
    public Translet translate(String name, Map<String, Object> attributeMap) {
        return translate(name, null, null, attributeMap, null);
    }

    @Override
    public Translet translate(String name, MethodType method) {
        return translate(name, method, null, null, null);
    }

    @Override
    public Translet translate(String name, MethodType method, ParameterMap parameterMap) {
        return translate(name, method, parameterMap, null, null);
    }

    @Override
    public Translet translate(String name, MethodType method, Map<String, Object> attributeMap) {
        return translate(name, method, null, attributeMap, null);
    }

    @Override
    public Translet translate(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap, String body) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (!isExposable(name)) {
            log.error("Unavailable translet: " + name);
            return null;
        }
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug(getServiceName() + " is paused, so did not execute translet: " + name);
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
            activity.setParameterMap(parameterMap);
            activity.setAttributeMap(attributeMap);
            activity.setBody(body);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new AspectranServiceException("An error occurred while processing translet: " + name, e);
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
        return translet;
    }

    @Override
    public String render(String templateId) {
        return render(templateId, null, null);
    }

    @Override
    public String render(String templateId, ParameterMap parameterMap) {
        return render(templateId, parameterMap, null);
    }

    @Override
    public String render(String templateId, Map<String, Object> attributeMap) {
        return render(templateId, null, attributeMap);
    }

    @Override
    public String render(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug(getServiceName() + " is paused, so did not execute template: " + templateId);
                }
                return null;
            } else {
                pauseTimeout = 0L;
            }
        }

        try {
            InstantActivity activity = new InstantActivity(getActivityContext(), parameterMap, attributeMap);
            activity.setSessionAdapter(newSessionAdapter());

            getActivityContext().getTemplateRenderer().render(templateId, activity);

            return activity.getResponseAdapter().getWriter().toString();
        } catch (Exception e) {
            throw new AspectranServiceException("An error occurred while rendering template: " + templateId, e);
        }
    }

    /**
     * Returns a new instance of {@code DefaultEmbeddedAspectran}.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code DefaultEmbeddedAspectran}
     */
    static DefaultEmbeddedAspectran create(AspectranConfig aspectranConfig) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootFile = contextConfig.getRootFile();
        if (!StringUtils.hasText(rootFile) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setRootFile(DEFAULT_APP_CONFIG_ROOT_FILE);
        }

        DefaultEmbeddedAspectran aspectran = new DefaultEmbeddedAspectran();
        aspectran.prepare(aspectranConfig);
        setServiceStateListener(aspectran);
        return aspectran;
    }

    private static void setServiceStateListener(final DefaultEmbeddedAspectran aspectran) {
        aspectran.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                aspectran.initSessionManager();
                aspectran.pauseTimeout = 0;
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
                    log.warn("Pause timeout in milliseconds needs to be set " +
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
