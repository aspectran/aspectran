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
package com.aspectran.jetty.server.handler;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.logging.LoggingGroupHelper;
import com.aspectran.utils.wildcard.IncludeExcludeParameters;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Created: 2025-03-27</p>
 */
public class PathBasedLoggingGroupHandler extends Handler.Wrapper {

    private Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandler(Handler handler) {
        super(handler);
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
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        Handler next = getHandler();
        if (next == null) {
            return false;
        }

        String groupName = resolveGroupName(request);
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }

        return next.handle(request, response, callback);
    }

    protected String resolveGroupName(@NonNull Request request) {
        String groupName = null;
        if (pathPatternsByGroupName != null) {
            String requestPath = request.getHttpURI().getPath();
            for (Map.Entry<String, IncludeExcludeWildcardPatterns> entry : pathPatternsByGroupName.entrySet()) {
                IncludeExcludeWildcardPatterns patterns = entry.getValue();
                if (patterns.matches(requestPath)) {
                    groupName = entry.getKey();
                    break;
                }
            }
        }
        return groupName;
    }

}
