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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;

import java.util.Map;

/**
 * Service facade used by the Aspectran Daemon runtime to execute named requests (translets)
 * programmatically.
 * <p>
 * Extends {@link CoreService} to expose the activity context, lifecycle, and common helpers
 * required to run background tasks. Implementations typically create a daemon-friendly
 * {@link com.aspectran.core.adapter.SessionAdapter SessionAdapter} and provide
 * {@code translate(...)} overloads to run a translet with optional attributes and parameters.
 * </p>
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface DaemonService extends CoreService {

    /**
     * Create and return a new session adapter from the daemon service.
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes the translet identified by {@code name} using the default request method
     * (typically {@link MethodType#GET}) and the given attributes and parameters.
     * <p>
     * Implementations should treat {@code attributeMap} and {@code parameterMap} as optional;
     * they may be {@code null} to indicate no attributes or parameters.
     * </p>
     * @param name the translet (request) name to execute
     * @param attributeMap attributes to expose to the request scope (may be {@code null})
     * @param parameterMap parameters to expose to the request (may be {@code null})
     * @return the resulting {@link Translet} bound to the execution
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     */
    Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the translet identified by {@code name} with the specified request {@code method},
     * applying the given attributes and parameters.
     * <p>
     * Implementations should treat {@code attributeMap} and {@code parameterMap} as optional;
     * they may be {@code null} to indicate no attributes or parameters.
     * </p>
     * @param name the translet (request) name to execute
     * @param method the request method to use (e.g., {@link MethodType#GET} or {@link MethodType#POST}); must not be {@code null}
     * @param attributeMap attributes to expose to the request scope (may be {@code null})
     * @param parameterMap parameters to expose to the request (may be {@code null})
     * @return the resulting {@link Translet} bound to the execution
     * @throws IllegalArgumentException if {@code name} is {@code null} or empty
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap);

}
