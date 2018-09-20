/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.AspectranRuntimeException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.SessionConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringOutputWriter;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.embed.activity.EmbeddedActivity;
import com.aspectran.embed.adapter.EmbeddedApplicationAdapter;
import com.aspectran.embed.adapter.EmbeddedSessionAdapter;

import java.util.Map;

import static com.aspectran.core.context.ActivityContext.BASE_DIR_PROPERTY_NAME;

/**
 * The Class AspectranEmbeddedService.
 *
 * @since 3.0.0
 */
class AspectranEmbeddedService extends AspectranCoreService implements EmbeddedService {

    private static final Log log = LogFactory.getLog(AspectranEmbeddedService.class);

    private SessionManager sessionManager;

    private long pauseTimeout = -1L;

    public AspectranEmbeddedService() {
        super(new EmbeddedApplicationAdapter());

        String basePath = SystemUtils.getProperty(BASE_DIR_PROPERTY_NAME);
        if (basePath != null) {
            setBasePath(basePath);
        }
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setGroupName("EMB");

        SessionConfig sessionConfig = getAspectranConfig().getParameters(AspectranConfig.session);
        if (sessionConfig != null) {
            sessionManager.setSessionConfig(sessionConfig);
        }

        sessionManager.initialize();
    }

    @Override
    public void beforeContextDestroy() {
        sessionManager.destroy();
        sessionManager = null;
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        SessionAgent agent = sessionManager.newSessionAgent();
        return new EmbeddedSessionAdapter(agent);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @return the {@code Translet} object
     */
    public Translet translet(String name) {
        return translet(name, null, null, null);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, ParameterMap parameterMap) {
        return translet(name, null, parameterMap, null);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        return translet(name, null, parameterMap, attributeMap);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, Map<String, Object> attributeMap) {
        return translet(name, null, null, attributeMap);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, MethodType method) {
        return translet(name, method, null, null);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, MethodType method, ParameterMap parameterMap) {
        return translet(name, method, parameterMap, null);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, MethodType method, Map<String, Object> attributeMap) {
        return translet(name, method, null, attributeMap);
    }

    /**
     * Executes the translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    @Override
    public Translet translet(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("AspectranEmbeddedService is paused, so did not execute the translet: " + name);
                }
                return null;
            } else {
                pauseTimeout = 0L;
            }
        }

        EmbeddedActivity activity = null;
        Translet translet = null;
        try {
            StringOutputWriter outputWriter = new StringOutputWriter();
            activity = new EmbeddedActivity(this, outputWriter);
            activity.setParameterMap(parameterMap);
            activity.setAttributeMap(attributeMap);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated: Cause: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new AspectranRuntimeException("An error occurred while processing an EmbeddedActivity", e);
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
        return translet;
    }

    /**
     * Evaluates the template without any provided variables.
     *
     * @param templateId the template id
     * @return the output string of the template
     */
    @Override
    public String template(String templateId) {
        return template(templateId, null, null);
    }

    /**
     * Evaluates the template with a set of parameters.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
    @Override
    public String template(String templateId, ParameterMap parameterMap) {
        return template(templateId, parameterMap, null);
    }

    /**
     * Evaluate the template with a set of parameters.
     *
     * @param templateId the template id
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    @Override
    public String template(String templateId, Map<String, Object> attributeMap) {
        return template(templateId, null, attributeMap);
    }

    /**
     * Evaluates the template with a set of parameters and attributes.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    @Override
    public String template(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        try {
            InstantActivity activity = new InstantActivity(getActivityContext(), parameterMap, attributeMap);
            activity.setSessionAdapter(newSessionAdapter());

            getActivityContext().getTemplateProcessor().process(templateId, activity);

            return activity.getResponseAdapter().getWriter().toString();
        } catch (Exception e) {
            throw new AspectranRuntimeException("An error occurred while processing a template", e);
        }
    }

    /**
     * Returns a new instance of {@code EmbeddedService}.
     *
     * @param rootConfigLocation the root configuration location
     * @return the instance of {@code EmbeddedService}
     * @throws AspectranServiceException the aspectran service exception
     */
    protected static EmbeddedService create(String rootConfigLocation) throws AspectranServiceException {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.updateRootConfigLocation(rootConfigLocation);
        return create(aspectranConfig);
    }

    /**
     * Returns a new instance of {@code EmbeddedService}.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code EmbeddedService}
     * @throws AspectranServiceException the aspectran service exception
     */
    protected static EmbeddedService create(AspectranConfig aspectranConfig) throws AspectranServiceException {
        ContextConfig contextConfig = aspectranConfig.getContextConfig();
        if (contextConfig == null) {
            contextConfig = aspectranConfig.newContextConfig();
        }

        String rootConfigLocation = contextConfig.getString(ContextConfig.root);
        if (rootConfigLocation == null || rootConfigLocation.isEmpty()) {
            if (contextConfig.getParameter(ContextConfig.parameters) == null) {
                contextConfig.putValue(ContextConfig.root, DEFAULT_ROOT_CONTEXT);
            }
        }

        AspectranEmbeddedService service = new AspectranEmbeddedService();
        service.prepare(aspectranConfig);

        setServiceStateListener(service);
        return service;
    }

    private static void setServiceStateListener(final AspectranEmbeddedService service) {
        service.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                service.pauseTimeout = 0;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis < 0L) {
                    throw new IllegalArgumentException("Pause timeout in milliseconds needs to be set to a value of greater than 0");
                }
                service.pauseTimeout = System.currentTimeMillis() + millis;
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
            }
        });
    }

}
