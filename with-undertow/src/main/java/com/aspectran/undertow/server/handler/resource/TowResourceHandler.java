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
package com.aspectran.undertow.server.handler.resource;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import com.aspectran.utils.wildcard.WildcardPattern;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An extension of Undertow's {@link ResourceHandler} that conditionally serves static resources.
 * <p>This handler uses include/exclude wildcard patterns to determine if a request path
 * corresponds to a static resource. If the path matches, it serves the resource. If it does
 * not match, it delegates the request to the next handler in the chain, allowing dynamic
 * requests (i.e., translets) to be processed.</p>
 */
public class TowResourceHandler extends ResourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(TowResourceHandler.class);

    private final HttpHandler next;

    private volatile IncludeExcludeWildcardPatterns pathPatterns;

    /**
     * Instantiates a new TowResourceHandler.
     * @param resourceManager the resource manager to use
     */
    public TowResourceHandler(ResourceManager resourceManager) {
        this(resourceManager, null);
    }

    /**
     * Instantiates a new TowResourceHandler.
     * @param resourceManager the resource manager to use
     * @param next the next handler in the chain
     */
    public TowResourceHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager);
        this.next = next;
    }

    /**
     * Instantiates a new TowResourceHandler.
     * @param resourceSupplier the resource supplier
     */
    public TowResourceHandler(ResourceSupplier resourceSupplier) {
        this(resourceSupplier, null);
    }

    /**
     * Instantiates a new TowResourceHandler.
     * @param resourceSupplier the resource supplier
     * @param next the next handler in the chain
     */
    public TowResourceHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier);
        this.next = next;
    }

    /**
     * Sets the include/exclude path patterns for static resources.
     * @param pathPatterns the resource path patterns
     */
    public void setPathPatterns(ResourcePathPatterns pathPatterns) {
        if (pathPatterns == null) {
            throw new IllegalArgumentException("pathPatterns must not be null");
        }
        this.pathPatterns = IncludeExcludeWildcardPatterns.of(pathPatterns, '/');
    }

    /**
     * Automatically detects and registers common static resource directories and files
     * from the resource manager's base path.
     * @param pathPrefix an optional prefix to apply to the detected resource paths
     * @throws IOException if an I/O error occurs while scanning for resources
     */
    public void autoDetect(String pathPrefix) throws IOException {
        Set<String> staticResources = null;
        if (getResourceManager() instanceof StaticResourceResolvable resolvable) {
            staticResources = resolvable.findStaticResources();
        }

        if (staticResources != null) {
            Set<WildcardPattern> patterns = new LinkedHashSet<>();
            if (pathPatterns != null && pathPatterns.hasIncludePatterns()) {
                for (WildcardPattern pattern : pathPatterns.getIncludePatterns()) {
                    boolean exists = false;
                    for (String resource : staticResources) {
                        if (resource.endsWith("/") && pattern.toString().startsWith(resource)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        patterns.add(pattern);
                    }
                }
            }
            for (String resource : staticResources) {
                if (StringUtils.hasLength(pathPrefix)) {
                    resource = pathPrefix + resource;
                }
                if (resource.endsWith("/")) {
                    patterns.add(WildcardPattern.compile(resource + "**", '/'));
                } else {
                    patterns.add(WildcardPattern.compile(resource));
                }
            }
            if (patterns.isEmpty()) {
                pathPatterns = null;
            } else {
                WildcardPattern[] includePatterns = patterns.toArray(new WildcardPattern[0]);
                WildcardPattern[] excludePatterns = (pathPatterns != null ? pathPatterns.getExcludePatterns() : null);
                pathPatterns = IncludeExcludeWildcardPatterns.of(includePatterns, excludePatterns);

                logger.info("TowResourceHandler includePatterns={}", Arrays.toString(includePatterns));
                if (excludePatterns != null) {
                    logger.info("TowResourceHandler excludePatterns={}", Arrays.toString(excludePatterns));
                }
            }
        }
    }

    /**
     * Returns whether any path patterns have been configured.
     * @return true if patterns are configured, false otherwise
     */
    public boolean hasPatterns() {
        return (pathPatterns != null);
    }

    /**
     * Handles the request by checking if the path matches the configured static resource patterns.
     * If it matches, the resource is served. Otherwise, the request is passed to the next handler.
     * @param exchange the HTTP server exchange
     * @throws Exception if an error occurs
     */
    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        String requestPath = exchange.getRequestPath();
        if (next == null || (pathPatterns != null && pathPatterns.matches(requestPath))) {
            super.handleRequest(exchange);
        } else {
            next.handleRequest(exchange);
        }
    }

}
