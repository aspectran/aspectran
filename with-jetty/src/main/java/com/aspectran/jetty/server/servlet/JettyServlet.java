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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ClassUtils;
import jakarta.servlet.Servlet;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import java.util.Map;

/**
 * <p>Created: 4/21/24</p>
 */
public class JettyServlet extends ServletHolder {

    private String[] mappings;

    @SuppressWarnings("unchecked")
    public JettyServlet(String name, String servletClass) throws ClassNotFoundException {
        this(name, (Class<? extends Servlet>) ClassUtils.getDefaultClassLoader().loadClass(servletClass));
    }

    public JettyServlet(String name, Class<? extends Servlet> servletClass) {
        super(name, servletClass);
    }

    public String[] getMappings() {
        return mappings;
    }

    public void setMappings(String[] mappings) {
        this.mappings = mappings;
    }

    public void setInitParams(Map<String, String> initParams) {
        setInitParameters(initParams);
    }

}
