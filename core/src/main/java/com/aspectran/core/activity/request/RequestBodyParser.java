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
package com.aspectran.core.activity.request;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameters;

/**
 * Utility class for parsing a request body into a {@link Parameters} object.
 * <p>
 * Provides static helper methods to interpret raw request content (typically
 * text or form-encoded data) and populate a parameters map that can be used
 * by an activity.
 * </p>
 *
 * @since 6.2.0
 */
public abstract class RequestBodyParser {

    /**
     * Parses the request body into a {@link Parameters}-compatible map.
     * @param body the raw request body as a string (e.g., form-encoded or JSON text)
     * @param requiredType the concrete type of {@link Parameters} to return
     * @param <T> a subclass of {@link Parameters}
     * @return a populated instance of {@code requiredType}, containing parameters
     *         parsed from the body
     * @throws IllegalArgumentException if parsing fails or the type is unsupported
     */
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
