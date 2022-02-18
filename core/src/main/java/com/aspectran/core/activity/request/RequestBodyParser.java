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
package com.aspectran.core.activity.request;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

/**
 * Provides convenient methods to parse the request body.
 *
 * @since 6.2.0
 */
public class RequestBodyParser {

    private RequestBodyParser() {
    }

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
