/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.ParameterKey;

import java.io.IOException;
import java.io.Reader;

public class RootParameters extends AbstractParameters {

    public static final ParameterKey aspectran;

    private static final ParameterKey[] parameterKeys;

    static {
        aspectran = new ParameterKey("aspectran", AspectranParameters.class);

        parameterKeys = new ParameterKey[] {
                aspectran
        };
    }

    public RootParameters() {
        super(parameterKeys);
    }

    public RootParameters(AspectranParameters aspectranParameters) {
        this();
        putValue(aspectran, aspectranParameters);
    }

    public RootParameters(Reader reader) throws IOException {
        this();
        AponReader.parse(reader, this);
    }

}
