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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.thymeleaf.web.servlet.IServletWebApplication;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * A Thymeleaf {@link IServletWebApplication} implementation backed by
 * Aspectran's {@link ApplicationAdapter}.
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityApplication implements IServletWebApplication {

    private final ActivityContext activityContext;

    private final ApplicationAdapter applicationAdapter;

    /**
     * Instantiates a new WebActivityApplication.
     * @param activityContext the activity context
     */
    public WebActivityApplication(@NonNull ActivityContext activityContext) {
        this.activityContext = activityContext;
        this.applicationAdapter = activityContext.getApplicationAdapter();
    }

    @Override
    public int getAttributeCount() {
        return (applicationAdapter != null ? applicationAdapter.getAttributeNames().size() : 0);
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return (applicationAdapter != null ? applicationAdapter.getAttributeNames() : Collections.emptySet());
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(getAllAttributeNames());
    }

    @Override
    public Object getAttributeValue(String name) {
        return (applicationAdapter != null ? applicationAdapter.getAttribute(name) : null);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        if (applicationAdapter != null) {
            applicationAdapter.setAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (applicationAdapter != null) {
            applicationAdapter.removeAttribute(name);
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        Assert.notNull(path, "Path cannot be null");
        if (applicationAdapter == null) {
            return null;
        }
        try {
            Path resourcePath = applicationAdapter.getRealPath(path);
            if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                return Files.newInputStream(resourcePath);
            }
            return activityContext.getClassLoader().getResourceAsStream(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public URL getResource(String path) {
        Assert.notNull(path, "Path cannot be null");
        if (applicationAdapter == null) {
            return null;
        }
        try {
            Path resourcePath = applicationAdapter.getRealPath(path);
            if (Files.exists(resourcePath)) {
                return resourcePath.toUri().toURL();
            }
            return activityContext.getClassLoader().getResource(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object getNativeServletContextObject() {
        return null;
    }

}
