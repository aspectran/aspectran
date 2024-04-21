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
package com.aspectran.core.service;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AcceptablesConfig;
import com.aspectran.utils.Assert;
import com.aspectran.utils.wildcard.PluralWildcardPattern;

/**
 * <p>Created: 4/21/24</p>
 */
public class ServiceAcceptables {

    private final PluralWildcardPattern acceptablesPattern;

    public ServiceAcceptables(AcceptablesConfig acceptablesConfig) {
        Assert.notNull(acceptablesConfig, "acceptablesConfig must not be null");
        String[] includePatterns = acceptablesConfig.getIncludePatterns();
        String[] excludePatterns = acceptablesConfig.getExcludePatterns();
        if ((includePatterns != null && includePatterns.length > 0) ||
            excludePatterns != null && excludePatterns.length > 0) {
            acceptablesPattern = new PluralWildcardPattern(includePatterns, excludePatterns,
                ActivityContext.NAME_SEPARATOR_CHAR);
        } else {
            acceptablesPattern = null;
        }
    }

    public boolean isAcceptable(String requestName) {
        return (acceptablesPattern == null || acceptablesPattern.matches(requestName));
    }

}
