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
package com.aspectran.netty.server.handler.logging;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.wildcard.IncludeExcludeParameters;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A Netty {@link io.netty.channel.ChannelHandler} that resolves and sets a logging group
 * for the current connection based on matching the HTTP request path against configured patterns.
 * <p>Supports fine-grained URL pattern routing to dedicated log files using include/exclude patterns.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class PathBasedLoggingGroupHandler extends ChannelInboundHandlerAdapter {

    private Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName;

    /**
     * Creates a new unconfigured path-based logging group handler.
     */
    public PathBasedLoggingGroupHandler() {
    }

    /**
     * Creates a new path-based logging group handler with the given pattern mapping.
     * @param pathPatternsByGroupName map of logging group names to APON formatted include/exclude patterns
     */
    public PathBasedLoggingGroupHandler(Map<String, String> pathPatternsByGroupName) {
        setPathPatternsByGroupName(pathPatternsByGroupName);
    }

    /**
     * Returns the compiled path patterns mapped by logging group name.
     * @return the mapping of group names to {@link IncludeExcludeWildcardPatterns}
     */
    public Map<String, IncludeExcludeWildcardPatterns> getPathPatternsByGroupName() {
        return pathPatternsByGroupName;
    }

    /**
     * Sets the mapping between logging group names and their corresponding path patterns.
     * @param pathPatternsByGroupName a map where the key is the logging group name and the value
     *                                is a string in APON format defining include/exclude patterns
     */
    public void setPathPatternsByGroupName(Map<String, String> pathPatternsByGroupName) {
        if (pathPatternsByGroupName != null) {
            Map<String, IncludeExcludeWildcardPatterns> map = new HashMap<>();
            try {
                for (Map.Entry<String, String> entry : pathPatternsByGroupName.entrySet()) {
                    String groupName = entry.getKey();
                    String apon = entry.getValue();
                    if (StringUtils.hasText(apon)) {
                        IncludeExcludeParameters includeExcludeParameters = new IncludeExcludeParameters(apon);
                        IncludeExcludeWildcardPatterns pathPatterns =
                                IncludeExcludeWildcardPatterns.of(includeExcludeParameters, '/');
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

    /**
     * Resolves the appropriate logging group name for the given request path by matching it
     * against configured patterns.
     * @param requestPath the request path to match
     * @return the matched logging group name, or {@code null} if no pattern matches
     */
    @Nullable
    public String resolveGroupName(@NonNull String requestPath) {
        if (pathPatternsByGroupName != null && !pathPatternsByGroupName.isEmpty()) {
            for (Map.Entry<String, IncludeExcludeWildcardPatterns> entry : pathPatternsByGroupName.entrySet()) {
                if (entry.getValue().matches(requestPath)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest request) {
            String uri = request.uri();
            int queryIndex = uri.indexOf('?');
            String path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);
            String groupName = resolveGroupName(path);
            if (groupName != null) {
                ChannelLoggingGroupHelper.setTo(ctx.channel(), groupName);
            }
        }
        ctx.fireChannelRead(msg);
    }

}
