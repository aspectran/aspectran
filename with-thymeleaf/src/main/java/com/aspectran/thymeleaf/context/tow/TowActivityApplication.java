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
package com.aspectran.thymeleaf.context.tow;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.utils.Assert;
import org.thymeleaf.web.servlet.IServletWebApplication;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * A Thymeleaf {@link IServletWebApplication} implementation for Aspectran's non-servlet
 * web environment, specifically backing the {@link TowActivity}.
 *
 * <p>This class provides application-level context to Thymeleaf by adapting an
 * underlying {@link ApplicationAdapter}. It handles application-scoped attributes and
 * resolves application-level resources using the file system path provided by the
 * {@code ApplicationAdapter}. For servlet-specific methods like
 * {@link #getNativeServletContextObject()}, it returns {@code null} to indicate
 * the absence of a true servlet environment.</p>
 *
 * <p>Created: 2025-10-07</p>
 */
public class TowActivityApplication implements IServletWebApplication {

    private final ActivityContext activityContext;

    private final ApplicationAdapter applicationAdapter;

    public TowActivityApplication(ActivityContext activityContext) {
        Assert.notNull(activityContext, "activityContext must not be null");
        this.activityContext = activityContext;
        this.applicationAdapter = activityContext.getApplicationAdapter();
    }

    @Override
    public Object getNativeServletContextObject() {
        return null;
    }

    @Override
    public int getAttributeCount() {
        return applicationAdapter.getAttributeNames().size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return applicationAdapter.getAttributeNames();
    }

    @Override
    public Object getAttributeValue(String name) {
        return applicationAdapter.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(applicationAdapter.getAttributeNames());
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        applicationAdapter.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        applicationAdapter.removeAttribute(name);
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

}
