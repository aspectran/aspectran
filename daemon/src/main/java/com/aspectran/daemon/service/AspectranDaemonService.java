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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SessionConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.activity.DaemonActivity;
import com.aspectran.daemon.adapter.DaemonApplicationAdapter;
import com.aspectran.daemon.adapter.DaemonSessionAdapter;

import java.util.Map;

import static com.aspectran.core.context.config.AspectranConfig.DEFAULT_APP_CONFIG_ROOT_FILE;

/**
 * The Class AspectranDaemonService.
 *
 * @since 5.1.0
 */
public class AspectranDaemonService extends AspectranCoreService implements DaemonService {

    private static final Log log = LogFactory.getLog(AspectranDaemonService.class);

    private SessionManager sessionManager;

    private SessionAgent sessionAgent;

    private volatile long pauseTimeout = -1L;

    public AspectranDaemonService() {
        super(new DaemonApplicationAdapter());

        determineBasePath();
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setWorkerName("DM" + this.hashCode() + "_");
        SessionConfig sessionConfig = getAspectranConfig().getSessionConfig();
        if (sessionConfig != null) {
            sessionManager.setSessionConfig(sessionConfig);
        }
        sessionManager.initialize();
        sessionAgent = sessionManager.newSessionAgent();
    }

    @Override
    public void beforeContextDestroy() {
        sessionManager.destroy();
        sessionManager = null;
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        return new DaemonSessionAdapter(sessionAgent);
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
    public Translet translate(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (!isExposable(name)) {
            log.error("Unexposable translet: " + name);
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

        DaemonActivity activity = null;
        Translet translet = null;
        try {
            activity = new DaemonActivity(this);
            activity.setParameterMap(parameterMap);
            activity.setAttributeMap(attributeMap);
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
    public boolean isExposable(String transletName) {
        return super.isExposable(transletName);
    }

    /**
     * Returns a new instance of {@code AspectranDaemonService}.
     *
     * @param aspectranConfig the parameters for aspectran configuration
     * @return the instance of {@code AspectranDaemonService}
     */
    public static AspectranDaemonService create(AspectranConfig aspectranConfig) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootFile = contextConfig.getRootFile();
        if (!StringUtils.hasText(rootFile)) {
            if (!contextConfig.hasAspectranParameters()) {
                contextConfig.setRootFile(DEFAULT_APP_CONFIG_ROOT_FILE);
            }
        }

        AspectranDaemonService service = new AspectranDaemonService();
        service.prepare(aspectranConfig);
        DaemonConfig daemonConfig = aspectranConfig.getDaemonConfig();
        if (daemonConfig != null) {
            applyDaemonConfig(service, daemonConfig);
        }
        setServiceStateListener(service);
        return service;
    }

    private static void applyDaemonConfig(AspectranDaemonService service, DaemonConfig daemonConfig) {
        ExposalsConfig exposalsConfig = daemonConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final AspectranDaemonService service) {
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
                    throw new IllegalArgumentException("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
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
