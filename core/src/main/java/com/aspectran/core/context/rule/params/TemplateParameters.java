/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class TemplateParameters extends AbstractParameters {

    public static final ParameterKey id;
    public static final ParameterKey engine;
    public static final ParameterKey name;
    public static final ParameterKey file;
    public static final ParameterKey resource;
    public static final ParameterKey url;
    public static final ParameterKey style;
    public static final ParameterKey content;
    public static final ParameterKey encoding;
    public static final ParameterKey noCache;

    private static final ParameterKey[] parameterKeys;

    static {
        id = new ParameterKey("id", ValueType.STRING);
        engine = new ParameterKey("engine", ValueType.STRING);
        name = new ParameterKey("name", ValueType.STRING);
        file = new ParameterKey("file", ValueType.STRING);
        resource = new ParameterKey("resource", ValueType.STRING);
        url = new ParameterKey("url", ValueType.STRING);
        style = new ParameterKey("style", ValueType.STRING);
        content = new ParameterKey("content", new String[] {"template"}, ValueType.TEXT);
        encoding = new ParameterKey("encoding", ValueType.STRING);
        noCache = new ParameterKey("noCache", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                id,
                engine,
                name,
                file,
                resource,
                url,
                style,
                content,
                encoding,
                noCache
        };
    }

    public TemplateParameters() {
        super(parameterKeys);
    }

}
