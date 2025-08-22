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
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.utils.Assert;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;

/**
 * Determines whether a request should be accepted based on configured wildcard patterns.
 * <p>This class uses an {@link IncludeExcludeWildcardPatterns} instance, initialized
 * from an {@link AcceptableConfig}, to match against request names.
 */
public class RequestAcceptor {

    private final IncludeExcludeWildcardPatterns acceptableRequestNamePatterns;

    /**
     * Instantiates a new RequestAcceptor.
     * @param acceptableConfig the configuration containing include/exclude patterns
     */
    public RequestAcceptor(AcceptableConfig acceptableConfig) {
        Assert.notNull(acceptableConfig, "acceptableConfig must not be null");
        if (acceptableConfig.hasPatterns()) {
            acceptableRequestNamePatterns = IncludeExcludeWildcardPatterns.of(
                    acceptableConfig, ActivityContext.NAME_SEPARATOR_CHAR);
        } else {
            acceptableRequestNamePatterns = null;
        }
    }

    /**
     * Checks if the given request name is acceptable according to the configured patterns.
     * <p>If no patterns are configured, all request names are considered acceptable.
     * @param requestName the name of the request to check
     * @return true if the request is acceptable, otherwise false
     */
    public boolean isAcceptable(String requestName) {
        return (acceptableRequestNamePatterns == null || acceptableRequestNamePatterns.matches(requestName));
    }

}
