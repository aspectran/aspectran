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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import com.aspectran.web.service.WebService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.thymeleaf.web.IWebApplication;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link IWebApplication} implementation for Aspectran's
 * non-servlet web environment, backed by {@link WebService}.
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityApplication implements IWebApplication {

    private final ActivityContext activityContext;

    private final WebService webService;

    /**
     * Instantiates a new WebActivityApplication.
     * @param activityContext the activity context
     */
    public WebActivityApplication(@NonNull ActivityContext activityContext) {
        this(activityContext, (activityContext.getMasterService() instanceof WebService ws ? ws : null));
    }

    /**
     * Instantiates a new WebActivityApplication.
     * @param activityContext the activity context
     * @param webService the web service
     */
    public WebActivityApplication(@NonNull ActivityContext activityContext, @Nullable WebService webService) {
        Assert.notNull(activityContext, "activityContext must not be null");
        this.activityContext = activityContext;
        this.webService = webService;
    }

    @Override
    public boolean containsAttribute(String name) {
        return (webService != null && webService.getAttribute(name) != null);
    }

    @Override
    public int getAttributeCount() {
        return (webService != null ? webService.getAttributeNames().size() : 0);
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return (webService != null ? webService.getAttributeNames() : Collections.emptySet());
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        if (webService == null) {
            return Collections.emptyMap();
        }
        Set<String> names = webService.getAttributeNames();
        Map<String, Object> map = new LinkedHashMap<>(names.size());
        for (String name : names) {
            map.put(name, webService.getAttribute(name));
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Object getAttributeValue(String name) {
        return (webService != null ? webService.getAttribute(name) : null);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        if (webService != null) {
            webService.setAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (webService != null) {
            webService.removeAttribute(name);
        }
    }

    @Override
    public boolean resourceExists(String path) {
        Assert.notNull(path, "Path cannot be null");
        try {
            if (webService != null && webService.getBasePath() != null) {
                Path resourcePath = Path.of(webService.getBasePath(), path);
                if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                    return true;
                }
            } else if (activityContext.getApplicationAdapter() != null) {
                Path resourcePath = activityContext.getApplicationAdapter().getRealPath(path);
                if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                    return true;
                }
            }
            return (activityContext.getClassLoader().getResource(path) != null);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        Assert.notNull(path, "Path cannot be null");
        try {
            if (webService != null && webService.getBasePath() != null) {
                Path resourcePath = Path.of(webService.getBasePath(), path);
                if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                    return Files.newInputStream(resourcePath);
                }
            } else if (activityContext.getApplicationAdapter() != null) {
                Path resourcePath = activityContext.getApplicationAdapter().getRealPath(path);
                if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                    return Files.newInputStream(resourcePath);
                }
            }
            return activityContext.getClassLoader().getResourceAsStream(path);
        } catch (Exception e) {
            return null;
        }
    }

}
