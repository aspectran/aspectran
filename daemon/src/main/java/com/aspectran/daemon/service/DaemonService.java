/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * The Interface DaemonService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface DaemonService extends CoreService {

    /**
     * Returns whether the translet can be exposed to the daemon service.
     * @param transletName the name of the translet to check
     * @return true if the translet can be exposed; false otherwise
     */
    boolean isExposable(String transletName);

    /**
     * Create and return a new session adapter from the daemon service.
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes the translet with the given parameters and attributes.
     * @param name the translet name
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, ParameterMap parameterMap, Map<String, Object> attributeMap);

    /**
     * Executes the translet with the given parameters and attributes.
     * @param name the translet name
     * @param method the request method
     * @param parameterMap the parameter map
     * @param attributeMap the attribute map
     * @return the {@code Translet} object
     */
    Translet translate(String name, MethodType method, ParameterMap parameterMap, Map<String, Object> attributeMap);

}
