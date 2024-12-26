/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.ServletContext;
import org.thymeleaf.web.servlet.IServletWebApplication;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class WebActivityApplication implements IServletWebApplication {

    private final ServletContext servletContext;

    WebActivityApplication(@NonNull ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return this.servletContext.getAttributeNames();
    }

    @Override
    public Object getAttributeValue(String name) {
        return this.servletContext.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        this.servletContext.setAttribute(name, value);
    }


    @Override
    public InputStream getResourceAsStream(String path) {
        Assert.notNull(path, "Path cannot be null");
        return this.servletContext.getResourceAsStream(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path cannot be null");
        return this.servletContext.getResource(path);
    }

    @Override
    public Object getNativeServletContextObject() {
        return this.servletContext;
    }

}
