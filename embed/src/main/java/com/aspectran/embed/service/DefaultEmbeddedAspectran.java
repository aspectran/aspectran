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
package com.aspectran.embed.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.InstantAction;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.InstantActivityException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.embed.activity.AspectranActivity;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Default implementation of the {@link EmbeddedAspectran} service.
 * <p>This class provides the concrete implementation for executing instant actions,
 * translating translets, and rendering templates within an embedded Aspectran instance.
 * It manages the lifecycle of activities and integrates with the core service holder
 * for global service management.
 */
public class DefaultEmbeddedAspectran extends AbstractEmbeddedAspectran {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEmbeddedAspectran.class);

    private volatile long pauseTimeout = -1L;

    DefaultEmbeddedAspectran() {
        super();
    }

    @Override
    public <V> V execute(InstantAction<V> instantAction) {
        if (checkPaused()) {
            return null;
        }
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
    public Translet translate(
            String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap) {
        return translate(name, method, attributeMap, parameterMap, null);
    }

    /**
     * Translates a request to a translet based on the provided parameters.
     * This method handles request routing, parameter binding, and execution.
     * <p>If the service is paused, returns {@code null}. If the request name is invalid,
     * logs an error and returns {@code null}. Otherwise, creates an {@code AspectranActivity}
     * with the specified request details, performs preparation and execution, and returns
     * the resulting translet.
     * @param name the name of the translet to execute; must not be null
     * @param method the request method (e.g., GET, POST); defaults to GET if null
     * @param attributeMap additional application-level attributes for the request
     * @param parameterMap request parameters (e.g., query or form parameters)
     * @param body the request body (if applicable, e.g., for POST)
     * @return the resulting translet, or {@code null} if the request is invalid or paused
     * @throws CoreServiceException if an error occurs during processing (wrapped in the exception)
     */
    @Override
    public Translet translate(
            String name, @Nullable MethodType method,
            @Nullable Map<String, Object> attributeMap, @Nullable ParameterMap parameterMap,
            @Nullable String body) {
        if (checkPaused()) {
            return null;
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (!isRequestAcceptable(name)) {
            logger.error("Unavailable translet: {}", name);
            return null;
        }

        AspectranActivity activity = new AspectranActivity(this);
        activity.setRequestName(name);
        activity.setRequestMethod(method != null ? method : MethodType.GET);
        activity.setAttributeMap(attributeMap);
        activity.setParameterMap(parameterMap);
        activity.setBody(body);
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
            if (activity.isExceptionRaised()) {
                t = activity.getRaisedException();
            } else {
                t = e;
            }
            throw new CoreServiceException("Error occurred while processing request: " +
                    activity.getFullRequestName(), t);
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

    /**
     * Renders a template using the provided template ID and optional attributes and parameters.
     * This method creates an {@code InstantActivity} to manage the rendering context,
     * sets the template and attributes, and executes the rendering logic.
     * <p>If the service is paused, returns {@code null}. Otherwise, the template is rendered
     * and the output is returned as a string.
     * @param templateId the ID of the template to render
     * @param attributeMap additional attributes to pass to the renderer
     * @param parameterMap request parameters to pass to the renderer (e.g., query params)
     * @return the rendered text as a string, or {@code null} if the service is paused
     * @throws CoreServiceException if an error occurs during rendering (wrapped in exception)
     */
    @Override
    public String render(String templateId, Map<String, Object> attributeMap, ParameterMap parameterMap) {
        if (checkPaused()) {
            return null;
        }
        try {
            InstantActivity activity = new InstantActivity(getActivityContext());
            activity.setSessionAdapter(newSessionAdapter());
            activity.setAttributeMap(attributeMap);
            activity.setParameterMap(parameterMap);
            return activity.perform(() -> {
                activity.getTemplateRenderer().render(templateId, activity);
                return activity.getResponseAdapter().getWriter().toString();
            });
        } catch (Exception e) {
            throw new CoreServiceException("Error while rendering template: " + templateId, e);
        }
    }

    /**
     * Returns a new instance of {@code DefaultEmbeddedAspectran} configured with the
     * provided configuration. This method initializes the service, sets up lifecycle
     * listeners, and configures internal state.
     * <p>Use this factory method to obtain a fully configured instance of the service.
     * The service will automatically register lifecycle callbacks for start/stop/pause/resume.
     * @param aspectranConfig the configuration for the aspectran
     * @return a configured instance of {@code DefaultEmbeddedAspectran}
     */
    @NonNull
    static DefaultEmbeddedAspectran create(AspectranConfig aspectranConfig) {
        DefaultEmbeddedAspectran aspectran = new DefaultEmbeddedAspectran();
        aspectran.configure(aspectranConfig);
        setServiceStateListener(aspectran);
        return aspectran;
    }

    /**
     * Determines whether the service is currently paused based on a timeout.
     * Returns {@code true} if the service is paused (i.e., paused for more than the
     * specified duration), otherwise returns {@code false}.
     * <p>Pausing is used to temporarily suspend processing (e.g., for maintenance).
     * When paused, no new requests are processed.
     * @return {@code true} if the service is paused, {@code false} otherwise
     */
    private boolean checkPaused() {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The service is paused, so the action is not executed.");
                }
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }
        return false;
    }

    private static void setServiceStateListener(@NonNull final DefaultEmbeddedAspectran aspectran) {
        aspectran.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(aspectran);
                aspectran.createSessionManager();
                aspectran.pauseTimeout = 0L;
            }

            @Override
            public void stopped() {
                aspectran.destroySessionManager();
                CoreServiceHolder.release(aspectran);
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    aspectran.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("The pause timeout in milliseconds must be greater than 0.");
                }
            }

            @Override
            public void paused() {
                aspectran.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                aspectran.pauseTimeout = 0L;
            }
        });
    }

}
