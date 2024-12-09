package com.aspectran.undertow.server.handler.logging;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.wildcard.WildcardPattern;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2024. 12. 10.</p>
 */
public class PathBasedLoggingGroupHandlerWrapper implements HandlerWrapper {

    private Map<String, List<WildcardPattern>> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandlerWrapper() {
    }

    public void setPathPatternsByGroupName(Map<String, String> pathPatternsByGroupName) {
        if (pathPatternsByGroupName != null) {
            Map<String, List<WildcardPattern>> map = new HashMap<>();
            for (Map.Entry<String, String> entry : pathPatternsByGroupName.entrySet()) {
                String groupName = entry.getKey();
                String[] arr = StringUtils.tokenize(entry.getValue(), ",;\t\r\n\f", true);
                if (arr.length > 0) {
                    List<WildcardPattern> pathPatterns = new ArrayList<>(arr.length);
                    for (String path : arr) {
                        pathPatterns.add(WildcardPattern.compile(path, ActivityContext.NAME_SEPARATOR_CHAR));
                    }
                    map.put(groupName, pathPatterns);
                }
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
