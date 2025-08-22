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
 * The main interface for the Aspectran Daemon service.
 * <p>This service provides a way to run Aspectran in a standalone, non-web environment,
 * allowing for the programmatic execution of translets. It extends {@link CoreService}
 * to provide access to the core application context and lifecycle management.
 *
 * <p>The primary method for interacting with this service is {@link #translate},
 * which triggers the execution of a specified translet.
 *
 * @since 5.1.0
 */
public interface DaemonService extends CoreService {

    /**
     * Creates and returns a new session adapter for the daemon environment.
     * This allows for session management in a non-web context.
     * @return a new {@link SessionAdapter}
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes the translet with the given name.
     * <p>The request method is determined by parsing the name; if the name is prefixed
     * with a method type (e.g., "POST /path/to/translet"), that method is used.
     * Otherwise, it defaults to {@link MethodType#GET}.
     * @param name the name of the translet to execute, optionally prefixed with a method
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return the result of the translet execution
     */
    Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the translet with the given name and request method.
     * @param name the name of the translet to execute
     * @param method the request method to use
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return the result of the translet execution
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap);

}
