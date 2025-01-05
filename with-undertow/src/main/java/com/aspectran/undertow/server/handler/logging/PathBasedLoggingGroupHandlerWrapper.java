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
package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.wildcard.IncludeExcludeParameters;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Created: 2024. 12. 10.</p>
 */
public class PathBasedLoggingGroupHandlerWrapper implements HandlerWrapper {

    private Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandlerWrapper() {
    }

    public void setPathPatternsByGroupName(Map<String, String> pathPatternsByGroupName) {
        if (pathPatternsByGroupName != null) {
            Map<String, IncludeExcludeWildcardPatterns> map = new HashMap<>();
            try {
                for (Map.Entry<String, String> entry : pathPatternsByGroupName.entrySet()) {
                    String groupName = entry.getKey();
                    String apon = entry.getValue();
                    if (StringUtils.hasText(apon)) {
                        IncludeExcludeParameters includeExcludeParameters = new IncludeExcludeParameters(apon);
                        IncludeExcludeWildcardPatterns pathPatterns = IncludeExcludeWildcardPatterns.of(includeExcludeParameters, '/');
                        map.put(groupName, pathPatterns);
                    }
                }
            } catch (AponParseException e) {
                throw new IllegalArgumentException("Include/Exclude patterns do not conform to the format", e);
            }
            this.pathPatternsByGroupName = (map.isEmpty() ? null : map);
        } else {
            this.pathPatternsByGroupName = null;
        }
    }

    @Override
    public HttpHandler wrap(HttpHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        return new PathBasedLoggingGroupHandler(handler, pathPatternsByGroupName);
    }

}
