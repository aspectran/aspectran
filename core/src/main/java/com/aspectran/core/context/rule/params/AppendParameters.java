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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Represents the parameters for an append rule.
 */
public class AppendParameters extends AbstractParameters {

    public static final ParameterKey file;
    public static final ParameterKey resource;
    public static final ParameterKey url;
    public static final ParameterKey format;
    public static final ParameterKey profile;
    public static final ParameterKey aspectran;

    private static final ParameterKey[] parameterKeys;

    static {
        file = new ParameterKey("file", ValueType.STRING);
        resource = new ParameterKey("resource", ValueType.STRING);
        url = new ParameterKey("url", ValueType.STRING);
        format = new ParameterKey("format", ValueType.STRING);
        profile = new ParameterKey("profile", ValueType.STRING);
        aspectran = new ParameterKey("aspectran", AspectranParameters.class);

        parameterKeys = new ParameterKey[] {
                file,
                resource,
                url,
                format,
                profile,
                aspectran
        };
    }

    public AppendParameters() {
        super(parameterKeys);
    }

}
