/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.activity.request;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameters;

/**
 * Provides convenient methods to parse the request body.
 *
 * @since 6.2.0
 */
public abstract class RequestBodyParser {

    @Nullable
    public static <T extends Parameters> T parseBodyAsParameters(String body, Class<T> requiredType)
            throws RequestParseException {
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        try {
            T parameters = ClassUtils.createInstance(requiredType);
            parameters.readFrom(body);
            return parameters;
        } catch (Exception e) {
            throw new RequestParseException("Failed to parse request body of APON format to required type [" +
                    requiredType.getName() + "]", e);
        }
    }

}
