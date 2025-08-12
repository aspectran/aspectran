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
 * Default implementation of {@link DaemonService} that executes named requests (translets)
 * within a daemon-oriented {@link com.aspectran.daemon.activity.DaemonActivity}.
 * <p>
 * Features:
 * </p>
 * <ul>
 *   <li>Accepts request names optionally prefixed with an HTTP-like method token
 *       (e.g., "POST foo/bar"). If present the token is parsed into a {@link MethodType}.</li>
 *   <li>Validates availability of the target translet via {@link #isRequestAcceptable(String)}.</li>
 *   <li>Builds and prepares a {@link com.aspectran.daemon.activity.DaemonActivity},
 *       performs it, and returns the bound {@link Translet}.</li>
 *   <li>Maps failures to {@link CoreServiceException} with a concise root-cause message.</li>
 *   <li>Honors a pause window via {@link #pauseTimeout}: if paused, the request is skipped.</li>
 * </ul>
 *
 * <p>Thread-safety: instances are typically used as singleton services.
 * The {@code pauseTimeout} flag is {@code volatile} to allow concurrent checks.</p>
 *
 * @since 5.1.0
 */
public class DefaultDaemonService extends AbstractDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDaemonService.class);

    /**
     * Pause control timeout in milliseconds.
     * <ul>
     *   <li>-1: paused indefinitely</li>
     *   <li>0: not paused</li>
     *   <li>> 0: paused until the given epoch millis</li>
     * </ul>
     */
    protected volatile long pauseTimeout = -1L;

    /**
     * Package-private constructor used by the daemon infrastructure.
     * Prefer building via DefaultDaemonServiceBuilder.
     */
    DefaultDaemonService() {
        super();
    }

    /**
     * Executes a translet by name using an optional method prefix in the name.
     * <p>
     * If {@code name} starts with a recognized {@link MethodType} followed by a space
     * (e.g., "POST foo/bar"), that method is extracted and used while the remainder
     * becomes the actual request name. Otherwise, the request method will be resolved
     * in the two-argument overload.
     * </p>
     * @param name the translet name, optionally prefixed with a request method token
     * @param attributeMap attributes to expose to the request (may be {@code null})
     * @param parameterMap parameters to expose to the request (may be {@code null})
     * @return the resulting {@link Translet}, or {@code null} if the service is paused
     *         or the target translet is not acceptable
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
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

    /**
     * Executes the given translet with an explicit request {@code method}.
     * <p>
     * Returns {@code null} when the service is currently paused or when the translet
     * is not acceptable according to configuration. On failure, the root cause is
     * extracted and wrapped in a {@link CoreServiceException} with a concise message.
     * </p>
     * @param name the translet name to execute (must not be {@code null})
     * @param method the request method to use; if {@code null}, defaults to {@link MethodType#GET}
     * @param attributeMap attributes to expose to the request (may be {@code null})
     * @param parameterMap parameters to expose to the request (may be {@code null})
     * @return the resulting {@link Translet}, or {@code null} if paused or unavailable
     * @throws IllegalArgumentException if {@code name} is {@code null}
     * @throws CoreServiceException if an error occurs while preparing or performing the activity
     */
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
            if (activity.isExceptionRaised()) {
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

    /**
     * Checks whether the service is currently paused and should skip execution.
     * <p>
     * Semantics of {@link #pauseTimeout}:
     * </p>
     * <ul>
     *   <li>-1: paused indefinitely</li>
     *   <li>0: not paused</li>
     *   <li>> 0: paused until the given epoch millis; when expired, automatically unpauses</li>
     * </ul>
     * @param name the request name (used for debug logging)
     * @return {@code true} if paused and the request should be skipped
     */
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
