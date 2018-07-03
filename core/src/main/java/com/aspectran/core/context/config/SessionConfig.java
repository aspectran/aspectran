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
package com.aspectran.core.context.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class SessionConfig extends AbstractParameters {

    public static final ParameterDefinition timeout;
    public static final ParameterDefinition evictionPolicy;
    public static final ParameterDefinition saveOnCreate;
    public static final ParameterDefinition saveOnInactiveEviction;
    public static final ParameterDefinition removeUnloadableSessions;
    public static final ParameterDefinition storeType;
    public static final ParameterDefinition fileStore;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        timeout = new ParameterDefinition("timeout", ParameterValueType.INT);
        evictionPolicy = new ParameterDefinition("evictionPolicy", ParameterValueType.INT);
        saveOnCreate = new ParameterDefinition("saveOnCreate", ParameterValueType.BOOLEAN);
        saveOnInactiveEviction = new ParameterDefinition("saveOnInactiveEviction", ParameterValueType.BOOLEAN);
        removeUnloadableSessions = new ParameterDefinition("removeUnloadableSessions", ParameterValueType.BOOLEAN);
        storeType = new ParameterDefinition("storeType", ParameterValueType.STRING);
        fileStore = new ParameterDefinition("fileStore", SessionFileStoreConfig.class);

        parameterDefinitions = new ParameterDefinition[] {
                timeout,
                evictionPolicy,
                saveOnCreate,
                saveOnInactiveEviction,
                removeUnloadableSessions,
                storeType,
                fileStore
        };
    }

    public SessionConfig() {
        super(parameterDefinitions);
    }

}
