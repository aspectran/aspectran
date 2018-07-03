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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class TemplateParameters extends AbstractParameters {

    public static final ParameterDefinition id;
    public static final ParameterDefinition engine;
    public static final ParameterDefinition name;
    public static final ParameterDefinition file;
    public static final ParameterDefinition resource;
    public static final ParameterDefinition url;
    public static final ParameterDefinition content;
    public static final ParameterDefinition style;
    public static final ParameterDefinition encoding;
    public static final ParameterDefinition noCache;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        id = new ParameterDefinition("id", ParameterValueType.STRING);
        engine = new ParameterDefinition("engine", ParameterValueType.STRING);
        name = new ParameterDefinition("name", ParameterValueType.STRING);
        file = new ParameterDefinition("file", ParameterValueType.STRING);
        resource = new ParameterDefinition("resource", ParameterValueType.STRING);
        url = new ParameterDefinition("url", ParameterValueType.STRING);
        content = new ParameterDefinition("content", ParameterValueType.TEXT);
        style = new ParameterDefinition("style", ParameterValueType.STRING);
        encoding = new ParameterDefinition("encoding", ParameterValueType.STRING);
        noCache = new ParameterDefinition("noCache", ParameterValueType.BOOLEAN);

        parameterDefinitions = new ParameterDefinition[] {
                id,
                engine,
                name,
                file,
                resource,
                url,
                content,
                style,
                encoding,
                noCache
        };
    }

    public TemplateParameters() {
        super(parameterDefinitions);
    }

}
