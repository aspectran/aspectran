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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ClassUtils;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.MultipartConfig;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import java.util.Map;

/**
 * A {@link ServletHolder} that holds a {@link Servlet} and its mappings.
 *
 * <p>Created: 4/21/24</p>
 */
public class JettyServlet extends ServletHolder {

    private String[] mappings;

    /**
     * Constructs a new {@code JettyServlet} instance by specifying the servlet's name
     * and the fully qualified class name of the servlet to be loaded. The provided
     * class name is resolved and loaded into a {@link Class} object.
     * @param name the name of the servlet
     * @param servletClass the fully qualified name of the servlet class to be loaded
     * @throws ClassNotFoundException if the specified servlet class cannot be found
     */
    @SuppressWarnings("unchecked")
    public JettyServlet(String name, String servletClass) throws ClassNotFoundException {
        this(name, (Class<? extends Servlet>) ClassUtils.getDefaultClassLoader().loadClass(servletClass));
    }

    /**
     * Constructs a new {@code JettyServlet} instance with the specified servlet name
     * and class. If the servlet class is annotated with {@link MultipartConfig},
     * a corresponding {@link MultipartConfigElement} is set for multipart handling.
     * @param name the name of the servlet
     * @param servletClass the {@link Class} object representing the servlet implementation
     */
    public JettyServlet(String name, Class<? extends Servlet> servletClass) {
        super(name, servletClass);
        MultipartConfig multipartConfig = servletClass.getAnnotation(MultipartConfig.class);
        if (multipartConfig != null) {
            setMultipartConfig(new MultipartConfigElement(multipartConfig));
        }
    }

    /**
     * Returns the mappings of this servlet.
     * @return the mappings of this servlet
     */
    public String[] getMappings() {
        return mappings;
    }

    /**
     * Sets the mappings of this servlet.
     * @param mappings the mappings of this servlet
     */
    public void setMappings(String[] mappings) {
        this.mappings = mappings;
    }

    /**
     * Sets the initialization parameters of this servlet.
     * @param initParams the initialization parameters of this servlet
     */
    public void setInitParams(Map<String, String> initParams) {
        setInitParameters(initParams);
    }

    /**
     * Returns the multipart configuration of this servlet.
     * @return the multipart configuration of this servlet
     */
    public MultipartConfigElement getMultipartConfig() {
        return getMultipartConfigElement();
    }

    /**
     * Sets the multipart configuration of this servlet.
     * @param multipartConfig the multipart configuration of this servlet
     */
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        getRegistration().setMultipartConfig(multipartConfig);
    }

}
