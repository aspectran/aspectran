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

import com.aspectran.utils.Assert;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.NonNull;
import org.thymeleaf.web.servlet.IServletWebApplication;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

/**
 * An implementation of {@link IServletWebApplication} for a Servlet-based web activity.
 *
 * <p>Created: 2026-09-02</p>
 */
public class ServletWebActivityApplication implements IServletWebApplication {

    private final ServletContext servletContext;

    /**
     * Instantiates a new ServletWebActivityApplication.
     * @param servletContext the servlet context
     */
    ServletWebActivityApplication(@NonNull ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return servletContext.getAttributeNames();
    }

    @Override
    public Object getAttributeValue(String name) {
        return servletContext.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        servletContext.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        servletContext.removeAttribute(name);
    }

    @Override
    public boolean containsAttribute(String name) {
        return (servletContext.getAttribute(name) != null);
    }

    @Override
    public int getAttributeCount() {
        return Collections.list(servletContext.getAttributeNames()).size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return Set.copyOf(Collections.list(servletContext.getAttributeNames()));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        Assert.notNull(path, "Path cannot be null");
        return servletContext.getResourceAsStream(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path cannot be null");
        return servletContext.getResource(path);
    }

    @Override
    public Object getNativeServletContextObject() {
        return servletContext;
    }

}
