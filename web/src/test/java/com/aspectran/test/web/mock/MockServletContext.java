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
package com.aspectran.test.web.mock;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of {@link ServletContext}.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class MockServletContext implements ServletContext {

    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String> initParameters = new HashMap<>();

    @Override public String getContextPath() { return ""; }
    @Override public ServletContext getContext(String uripath) { return null; }
    @Override public int getMajorVersion() { return 6; }
    @Override public int getMinorVersion() { return 0; }
    @Override public int getEffectiveMajorVersion() { return 6; }
    @Override public int getEffectiveMinorVersion() { return 0; }
    @Override public String getMimeType(String file) { return null; }
    @Override public Set<String> getResourcePaths(String path) { return Collections.emptySet(); }
    @Override public URL getResource(String path) throws MalformedURLException { return null; }
    @Override public InputStream getResourceAsStream(String path) { return null; }
    @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public jakarta.servlet.RequestDispatcher getNamedDispatcher(String name) { return null; }
    @Override public void log(String msg) {}
    @Override public void log(String message, Throwable throwable) {}
    @Override public String getRealPath(String path) { return null; }
    @Override public String getServerInfo() { return "MockServletContext"; }
    @Override public String getInitParameter(String name) { return initParameters.get(name); }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParameters.keySet()); }
    @Override public boolean setInitParameter(String name, String value) { initParameters.put(name, value); return true; }
    @Override public Object getAttribute(String name) { return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
    @Override public void setAttribute(String name, Object object) { attributes.put(name, object); }
    @Override public void removeAttribute(String name) { attributes.remove(name); }
    @Override public String getServletContextName() { return "Mock"; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, String className) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) { return null; }
    @Override public ServletRegistration.Dynamic addJspFile(String jspName, String jspFile) { return null; }
    @Override public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException { return null; }
    @Override public ServletRegistration getServletRegistration(String servletName) { return null; }
    @Override public Map<String, ? extends ServletRegistration> getServletRegistrations() { return Collections.emptyMap(); }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, String className) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) { return null; }
    @Override public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException { return null; }
    @Override public FilterRegistration getFilterRegistration(String filterName) { return null; }
    @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Collections.emptyMap(); }
    @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
    @Override public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {}
    @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return Collections.emptySet(); }
    @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return Collections.emptySet(); }
    @Override public void addListener(String className) {}
    @Override public <T extends EventListener> void addListener(T t) {}
    @Override public void addListener(Class<? extends EventListener> listenerClass) {}
    @Override public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException { return null; }
    @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }
    @Override public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
    @Override public void declareRoles(String... roleNames) {}
    @Override public String getVirtualServerName() { return "localhost"; }
    @Override public int getSessionTimeout() { return 30; }
    @Override public void setSessionTimeout(int sessionTimeout) {}
    @Override public String getRequestCharacterEncoding() { return "UTF-8"; }
    @Override public void setRequestCharacterEncoding(String encoding) {}
    @Override public String getResponseCharacterEncoding() { return "UTF-8"; }
    @Override public void setResponseCharacterEncoding(String encoding) {}

}
