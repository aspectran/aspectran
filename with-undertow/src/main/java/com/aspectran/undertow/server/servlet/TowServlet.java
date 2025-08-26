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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ClassUtils;
import io.undertow.servlet.api.ServletInfo;
import jakarta.servlet.Servlet;

import java.util.Map;

/**
 * Represents a servlet that can be added to a deployment.
 *
 * <p>Created: 2019-08-05</p>
 */
public class TowServlet extends ServletInfo {

    /**
     * Creates a new servlet with the specified name and servlet class name.
     * @param name the servlet name
     * @param servletClass the servlet class name
     * @throws ClassNotFoundException if the class is not found
     */
    @SuppressWarnings("unchecked")
    public TowServlet(String name, String servletClass) throws ClassNotFoundException {
        this(name, (Class<? extends Servlet>)ClassUtils.getDefaultClassLoader().loadClass(servletClass));
    }

    /**
     * Creates a new servlet with the specified name and servlet class.
     * @param name the servlet name
     * @param servletClass the servlet class
     */
    public TowServlet(String name, Class<? extends Servlet> servletClass) {
        super(name, servletClass);
    }

    /**
     * Sets the URL patterns to which this servlet applies.
     * @param mappings the URL patterns
     */
    public void setMappings(String[] mappings) {
        if (mappings != null) {
            super.addMappings(mappings);
        }
    }

    /**
     * Sets the initialization parameters for this servlet.
     * @param initParams the initialization parameters
     */
    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParam(entry.getKey(), entry.getValue());
            }
        }
    }

}
