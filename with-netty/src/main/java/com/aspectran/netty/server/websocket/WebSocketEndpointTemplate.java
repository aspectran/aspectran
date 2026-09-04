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
package com.aspectran.netty.server.websocket;

import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses and matches JSR-356 URI templates (e.g. {@code /nodes/{nodeId}/appmon/websocket/{token}}).
 *
 * <p>Created: 2026-09-03</p>
 */
public class WebSocketEndpointTemplate implements Comparable<WebSocketEndpointTemplate> {

    private final String pattern;

    private final NettyWebSocketListener listener;

    private final String[] segments;

    private final String[] variableNames;

    private final int literalCount;

    private final boolean template;

    /**
     * Creates a new WebSocket endpoint template with a URI pattern and associated listener.
     * @param pattern the URI template pattern (e.g. {@code /chat/{room}})
     * @param listener the WebSocket listener to dispatch to
     */
    public WebSocketEndpointTemplate(@NonNull String pattern, @NonNull NettyWebSocketListener listener) {
        Assert.notNull(pattern, "pattern must not be null");
        Assert.notNull(listener, "listener must not be null");
        this.pattern = (pattern.startsWith("/") ? pattern : "/" + pattern);
        this.listener = listener;

        String[] parts = splitPath(this.pattern);
        this.segments = new String[parts.length];
        this.variableNames = new String[parts.length];
        boolean hasVar = false;
        int literals = 0;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("{") && part.endsWith("}") && part.length() > 2) {
                this.segments[i] = null;
                this.variableNames[i] = part.substring(1, part.length() - 1);
                hasVar = true;
            } else {
                this.segments[i] = part;
                this.variableNames[i] = null;
                literals++;
            }
        }
        this.template = hasVar;
        this.literalCount = literals;
    }

    /**
     * Returns the normalized URI template pattern string.
     * @return the pattern
     */
    @NonNull
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the associated WebSocket listener.
     * @return the listener
     */
    @NonNull
    public NettyWebSocketListener getListener() {
        return listener;
    }

    /**
     * Returns whether this template contains dynamic path variables.
     * @return {@code true} if dynamic template variables exist; {@code false} if exact literal path
     */
    public boolean isTemplate() {
        return template;
    }

    /**
     * Matches a request path against this template and extracts path parameters.
     * @param path the request path to match
     * @return a map of path variable names to decoded values, or {@code null} if no match
     */
    @Nullable
    public Map<String, String> match(@NonNull String path) {
        int queryIndex = path.indexOf('?');
        String cleanPath = (queryIndex != -1 ? path.substring(0, queryIndex) : path);
        String[] reqParts = splitPath(cleanPath);
        if (reqParts.length != segments.length) {
            return null;
        }

        Map<String, String> params = (template ? new LinkedHashMap<>() : Collections.emptyMap());
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            String reqSeg = reqParts[i];
            if (seg != null) {
                if (!seg.equals(reqSeg)) {
                    return null;
                }
            } else {
                if (reqSeg.isEmpty()) {
                    return null;
                }
                String decoded = URLDecoder.decode(reqSeg, StandardCharsets.UTF_8);
                params.put(variableNames[i], decoded);
            }
        }
        return params;
    }

    @Override
    public int compareTo(@NonNull WebSocketEndpointTemplate other) {
        // More literal segments take higher precedence
        int cmp = Integer.compare(other.literalCount, this.literalCount);
        if (cmp != 0) {
            return cmp;
        }
        // Longer path takes precedence
        cmp = Integer.compare(other.segments.length, this.segments.length);
        if (cmp != 0) {
            return cmp;
        }
        return this.pattern.compareTo(other.pattern);
    }

    private static String[] splitPath(String path) {
        if (path.isEmpty() || "/".equals(path)) {
            return new String[0];
        }
        String p = (path.startsWith("/") ? path.substring(1) : path);
        if (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p.split("/+");
    }

}
