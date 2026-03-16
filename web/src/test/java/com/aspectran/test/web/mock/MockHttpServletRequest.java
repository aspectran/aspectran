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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mock implementation of {@link HttpServletRequest}.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String[]> parameters = new HashMap<>();
    private String method = "GET";
    private String requestURI;
    private String contextPath = "";
    private String servletPath = "";
    private String pathInfo;
    private String queryString;
    private String characterEncoding = "UTF-8";
    private HttpSession session;

    public void setMethod(String method) { this.method = method; }
    public void setRequestURI(String requestURI) { this.requestURI = requestURI; }
    public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    public void setServletPath(String servletPath) { this.servletPath = servletPath; }
    public void setPathInfo(String pathInfo) { this.pathInfo = pathInfo; }
    public void setQueryString(String queryString) { this.queryString = queryString; }
    public void setParameter(String name, String value) { this.parameters.put(name, new String[]{value}); }
    public void setSession(HttpSession session) { this.session = session; }

    @Override public String getAuthType() { return null; }
    @Override public Cookie[] getCookies() { return new Cookie[0]; }
    @Override public long getDateHeader(String name) { return 0; }
    @Override public String getHeader(String name) { return null; }
    @Override public Enumeration<String> getHeaders(String name) { return Collections.emptyEnumeration(); }
    @Override public Enumeration<String> getHeaderNames() { return Collections.emptyEnumeration(); }
    @Override public int getIntHeader(String name) { return 0; }
    @Override public String getMethod() { return method; }
    @Override public String getPathInfo() { return pathInfo; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getContextPath() { return contextPath; }
    @Override public String getQueryString() { return queryString; }
    @Override public String getRemoteUser() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public Principal getUserPrincipal() { return null; }
    @Override public String getRequestedSessionId() { return null; }
    @Override public String getRequestURI() { return requestURI; }
    @Override public StringBuffer getRequestURL() { return new StringBuffer(requestURI); }
    @Override public String getServletPath() { return servletPath; }
    @Override public HttpSession getSession(boolean create) { return session; }
    @Override public HttpSession getSession() { return session; }
    @Override public String changeSessionId() { return null; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }
    @Override public boolean authenticate(HttpServletResponse response) throws IOException, ServletException { return false; }
    @Override public void login(String username, String password) throws ServletException {}
    @Override public void logout() throws ServletException {}
    @Override public Collection<jakarta.servlet.http.Part> getParts() throws IOException, ServletException { return null; }
    @Override public jakarta.servlet.http.Part getPart(String name) throws IOException, ServletException { return null; }
    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException { return null; }
    @Override public Object getAttribute(String name) { return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
    @Override public String getCharacterEncoding() { return characterEncoding; }
    @Override public void setCharacterEncoding(String env) throws UnsupportedEncodingException { this.characterEncoding = env; }
    @Override public int getContentLength() { return 0; }
    @Override public long getContentLengthLong() { return 0; }
    @Override public String getContentType() { return null; }
    @Override public ServletInputStream getInputStream() throws IOException { return null; }
    @Override public String getParameter(String name) { String[] vals = parameters.get(name); return (vals != null && vals.length > 0 ? vals[0] : null); }
    @Override public Enumeration<String> getParameterNames() { return Collections.enumeration(parameters.keySet()); }
    @Override public String[] getParameterValues(String name) { return parameters.get(name); }
    @Override public Map<String, String[]> getParameterMap() { return parameters; }
    @Override public String getProtocol() { return "HTTP/1.1"; }
    @Override public String getScheme() { return "http"; }
    @Override public String getServerName() { return "localhost"; }
    @Override public int getServerPort() { return 80; }
    @Override public BufferedReader getReader() throws IOException { return null; }
    @Override public String getRemoteAddr() { return "127.0.0.1"; }
    @Override public String getRemoteHost() { return "localhost"; }
    @Override public void setAttribute(String name, Object o) { attributes.put(name, o); }
    @Override public void removeAttribute(String name) { attributes.remove(name); }
    @Override public Locale getLocale() { return Locale.getDefault(); }
    @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singletonList(Locale.getDefault())); }
    @Override public boolean isSecure() { return false; }
    @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
    @Override public int getRemotePort() { return 0; }
    @Override public String getLocalName() { return "localhost"; }
    @Override public String getLocalAddr() { return "127.0.0.1"; }
    @Override public int getLocalPort() { return 80; }
    @Override public ServletContext getServletContext() { return null; }
    @Override public AsyncContext startAsync() throws IllegalStateException { return null; }
    @Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException { return null; }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public AsyncContext getAsyncContext() { return null; }
    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
    @Override public String getRequestId() { return null; }
    @Override public String getProtocolRequestId() { return null; }
    @Override public ServletConnection getServletConnection() { return null; }

}
