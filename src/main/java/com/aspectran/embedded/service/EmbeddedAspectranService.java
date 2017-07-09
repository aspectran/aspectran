/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.embedded.service;

import java.util.Map;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.Component;
import com.aspectran.core.component.session.BasicSession;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.AspectranRuntimeException;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.AspectranContextConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringOutputWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.embedded.activity.EmbeddedActivity;
import com.aspectran.embedded.adapter.EmbeddedApplicationAdapter;
import com.aspectran.embedded.adapter.EmbeddedSessionAdapter;

/**
 * The Class EmbeddedAspectranService.
 *
 * @since 3.0.0
 */
public class EmbeddedAspectranService extends BasicAspectranService {

    private static final Log log = LogFactory.getLog(EmbeddedAspectranService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "classpath:root-config.xml";

    private SessionManager sessionManager;

    private String sessionId;

    private long pauseTimeout = -1L;

    public EmbeddedAspectranService() {
        super(new EmbeddedApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager("EMB");
        ((Component)sessionManager).initialize();

        sessionId = sessionManager.newSessionId(hashCode());

        final SessionScopeAdvisor sessionScopeAdvisor = SessionScopeAdvisor.create(getActivityContext());
        if (sessionScopeAdvisor != null) {
            sessionManager.addEventListener(new SessionListener() {
                @Override
                public void sessionCreated(BasicSession session) {
                    sessionScopeAdvisor.executeBeforeAdvice();
                }

                @Override
                public void sessionDestroyed(BasicSession session) {
                    sessionScopeAdvisor.executeAfterAdvice();
                }
            });
        }
    }

    @Override
    public void beforeContextDestroy() {
        ((Component)sessionManager).destroy();
        sessionManager = null;
    }

    public SessionAdapter newSessionAdapter() {
        BasicSession session = sessionManager.getSession(sessionId);
        return new EmbeddedSessionAdapter(session);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @return the {@code Translet} object
     */
    public Translet translet(String name) {
        return translet(name, null, null, null);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, ParameterMap parameterMap) {
        return translet(name, null, parameterMap, null);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        return translet(name, null, parameterMap, attributeMap);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, Map<String, Object> attributeMap) {
        return translet(name, null, null, attributeMap);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param method the request method
     * @return the {@code Translet} object
     */
    public Translet translet(String name, MethodType method) {
        return translet(name, method, null, null);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, MethodType method, ParameterMap parameterMap) {
        return translet(name, method, parameterMap, null);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, MethodType method, Map<String, Object> attributeMap) {
        return translet(name, method, null, attributeMap);
    }

    /**
     * Run a translet.
     *
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    public Translet translet(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                log.warn("AspectranService has been paused, so did not run the translet \"" + name + "\"");
                return null;
            } else {
                pauseTimeout = 0L;
            }
        }

        EmbeddedActivity activity = null;
        Translet translet = null;
        StringOutputWriter outputWriter = new StringOutputWriter();

        try {
            activity = new EmbeddedActivity(this, outputWriter);
            activity.setParameterMap(parameterMap);
            activity.setAttributeMap(attributeMap);
            activity.prepare(name, method);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Translet activity was terminated");
            }
        } catch (Exception e) {
            throw new AspectranRuntimeException("An error occurred while executing a translet", e);
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }

        return translet;
    }

    /**
     * Evaluate the template without any provided variables.
     *
     * @param templateId the template id
     * @return the output string of the template
     */
    public String template(String templateId) {
        return template(templateId, null, null);
    }

    /**
     * Evaluate the template with a set of parameters.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @return the output string of the template
     */
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
    public String template(String templateId, Map<String, Object> attributeMap) {
        return template(templateId, null, attributeMap);
    }

    /**
     * Evaluate the template with a set of parameters and a set of attributes.
     *
     * @param templateId the template id
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the output string of the template
     */
    public String template(String templateId, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        try {
            InstantActivity activity = new InstantActivity(getActivityContext(), newSessionAdapter());
            if (parameterMap != null) {
                activity.setParameterMap(parameterMap);
            }
            if (attributeMap != null) {
                activity.setAttributeMap(attributeMap);
            }
            activity.adapt();

            getActivityContext().getTemplateProcessor().process(templateId, activity);

            return activity.getResponseAdapter().getWriter().toString();
        } catch (Exception e) {
            throw new AspectranRuntimeException("An error occurred while processing a template", e);
        }
    }

    /**
     * Returns a new instance of EmbeddedAspectranService.
     *
     * @param rootContext the root configuration file
     * @return the embedded aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static EmbeddedAspectranService create(String rootContext) throws AspectranServiceException {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.updateRootContext(rootContext);
        return create(aspectranConfig);
    }

    /**
     * Returns a new instance of EmbeddedAspectranService.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the embedded aspectran service
     * @throws AspectranServiceException the aspectran service exception
     */
    public static EmbeddedAspectranService create(AspectranConfig aspectranConfig) throws AspectranServiceException {
        Parameters contextParameters = aspectranConfig.getAspectranContextConfig();
        if (contextParameters == null) {
            contextParameters = aspectranConfig.newAspectranContextConfig();
        }

        String rootContext = contextParameters.getString(AspectranContextConfig.root);
        if (rootContext == null || rootContext.isEmpty()) {
            if (contextParameters.getParameter(AspectranContextConfig.parameters) == null) {
                contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
            }
        }

        EmbeddedAspectranService aspectranService = new EmbeddedAspectranService();
        aspectranService.prepare(aspectranConfig);

        setServiceLifeCycleListener(aspectranService);

        return aspectranService;
    }

    private static void setServiceLifeCycleListener(final EmbeddedAspectranService aspectranService) {
        aspectranService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                aspectranService.pauseTimeout = 0;
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
                aspectranService.pauseTimeout = System.currentTimeMillis() + millis;
            }

            @Override
            public void paused() {
                aspectranService.pauseTimeout = -1L;
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
