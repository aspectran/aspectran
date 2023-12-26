/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.undertow.server.resource;

import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.wildcard.PluralWildcardPattern;
import com.aspectran.utils.wildcard.WildcardPattern;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class StaticResourceHandler extends ResourceHandler {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHandler.class);

    private volatile PluralWildcardPattern resourcePathPatterns;

    public StaticResourceHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public StaticResourceHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public StaticResourceHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public StaticResourceHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public void setResourcePathPatterns(ResourcePathPatterns resourcePathPatterns) {
        String[] includePatterns = resourcePathPatterns.getIncludePatterns();
        String[] excludePatterns = resourcePathPatterns.getExcludePatterns();
        this.resourcePathPatterns = new PluralWildcardPattern(includePatterns, excludePatterns, '/');
    }

    public void autoDetect() throws IOException {
        if (getResourceManager() instanceof PathResourceManager) {
            Path base = ((PathResourceManager)getResourceManager()).getBasePath();
            Set<String> staticResources = findStaticResources(base);

            Set<WildcardPattern> patterns = new LinkedHashSet<>();
            if (resourcePathPatterns != null && resourcePathPatterns.hasIncludePatterns()) {
                for (WildcardPattern pattern : resourcePathPatterns.getIncludePatterns()) {
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
                if (resource.endsWith("/")) {
                    patterns.add(WildcardPattern.compile(resource + "**", '/'));
                } else {
                    patterns.add(WildcardPattern.compile(resource));
                }
            }
            if (patterns.isEmpty()) {
                resourcePathPatterns = null;
            } else {
                WildcardPattern[] includePatterns = patterns.toArray(new WildcardPattern[0]);
                WildcardPattern[] excludePatterns = (resourcePathPatterns != null ? resourcePathPatterns.getExcludePatterns() : null);
                resourcePathPatterns = new PluralWildcardPattern(includePatterns, excludePatterns);

                logger.info("StaticResourceHandler includePatterns=" + Arrays.toString(includePatterns));
                if (excludePatterns != null) {
                    logger.info("StaticResourceHandler excludePatterns=" + Arrays.toString(excludePatterns));
                }
            }
        }
    }

    private Set<String> findStaticResources(Path base) throws IOException {
        Set<String> resources = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base)) {
            for (Path child : stream) {
                if ("WEB-INF".equals(child.getFileName().toString())) {
                    resources.add("/" + child.getFileName() + "/");
                } else {
                    if (Files.isDirectory(child)) {
                        findStaticResourceDirs(child, "/" + child.getFileName() + "/", resources);
                    } else {
                        resources.add("/" + child.getFileName());
                    }
                }
            }
        }
        return resources;
    }

    private void findStaticResourceDirs(Path parent, String prefix, Set<String> resources) throws IOException {
        Set<Path> children = new HashSet<>();
        boolean found = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    children.add(child);
                } else {
                    children.clear();
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            resources.add(prefix);
        } else if (!children.isEmpty()) {
            for (Path child: children) {
                findStaticResourceDirs(child, prefix + child.getFileName() + "/", resources);
            }
        }
    }

    public boolean hasPatterns() {
        return (resourcePathPatterns != null);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String requestPath = exchange.getRequestPath();
        if (resourcePathPatterns != null) {
            if (resourcePathPatterns.matches(requestPath)) {
                super.handleRequest(exchange);
            }
        }
    }

}
