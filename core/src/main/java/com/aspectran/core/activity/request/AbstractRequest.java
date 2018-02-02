/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity.request;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.request.parameter.FileParameterMap;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * The Class AbstractRequest.
 *
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

    private MethodType requestMethod;

    private MultiValueMap<String, String> headers;

    private ParameterMap parameterMap;

    private FileParameterMap fileParameterMap;

    private Locale locale;

    private TimeZone timeZone;

    private boolean maxLengthExceeded;

    protected Locker locker = new Locker();

    public AbstractRequest() {
    }

    public AbstractRequest(Map<String, String[]> parameterMap) {
        if (parameterMap != null && !parameterMap.isEmpty()) {
            this.parameterMap = new ParameterMap(parameterMap);
        }
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    protected void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.</p>
     *
     * @param name the name of the response header whose value to return
     * @return the value of the response header with the given name,
     *         or {@code null} if no header with the given name has been set
     *         on this response
     */
    public String getHeader(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return touchHeaders().getFirst(name);
        }
    }

    /**
     * Returns the values of the response header with the given name.
     *
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    public Collection<String> getHeaders(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return touchHeaders().get(name);
        }
    }

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    public Collection<String> getHeaderNames() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return touchHeaders().keySet();
        }
    }

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     *
     * @param name the header name
     * @return {@code true} if the named response header
     *         has already been set; {@code false} otherwise
     */
    public boolean containsHeader(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            List<String> values = touchHeaders().get(name);
            return (values != null && !values.isEmpty());
        }
    }

    /**
     * Set the given single header value under the given header name.
     *
     * @param name the header name
     * @param value the header value to set
     */
    public void setHeader(String name, String value) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchHeaders().set(name, value);
        }
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    public void addHeader(String name, String value) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchHeaders().add(name, value);
        }
    }

    /**
     * Returns a map of the request headers that can be modified.
     *
     * @return an {@code MultiValueMap} object, may be {@code null}
     */
    public MultiValueMap<String, String> getAllHeaders() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return touchHeaders();
        }
    }

    /**
     * Returns a map of the request headers that can be modified.
     * If not yet instantiated then create a new one.
     *
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
    protected MultiValueMap<String, String> touchHeaders() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>(12);
        }
        return headers;
    }

    protected boolean isHeadersInstantiated() {
        return (headers != null);
    }

    public String getParameter(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (parameterMap != null ? parameterMap.getParameter(name) : null);
        }
    }

    public String[] getParameterValues(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (parameterMap != null ? parameterMap.getParameterValues(name) : null);
        }
    }

    public Enumeration<String> getParameterNames() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (parameterMap != null ? parameterMap.getParameterNames() : null);
        }
    }

    public void setParameter(String name, String value) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchParameterMap().setParameter(name, value);
        }
    }

    public void setParameter(String name, String[] values) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchParameterMap().put(name, values);
        }
    }

    public Map<String, Object> getAllParameters() {
        return Collections.unmodifiableMap(this.parameterMap);
    }

    public Map<String, Object> copyAllParameters() {
        Map<String, Object> params = new LinkedHashMap<>(parameterMap.size());
        fillAllParameters(params);
        return params;
    }

    public void fillAllParameters(Map<String, Object> targetParameters) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (this.parameterMap != null) {
                for (Map.Entry<String, String[]> entry : this.parameterMap.entrySet()) {
                    String name = entry.getKey();
                    String[] values = entry.getValue();
                    if (values.length == 1) {
                        targetParameters.put(name, values[0]);
                    } else {
                        targetParameters.put(name, values);
                    }
                }
            }
        }
    }

    private ParameterMap touchParameterMap() {
        if (this.parameterMap == null) {
            this.parameterMap = new ParameterMap();
        }
        return this.parameterMap;
    }

    public FileParameter getFileParameter(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (fileParameterMap != null ? fileParameterMap.getFileParameter(name) : null);
        }
    }

    public FileParameter[] getFileParameterValues(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (fileParameterMap != null ? fileParameterMap.getFileParameterValues(name) : null);
        }
    }

    public void removeFileParameter(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (fileParameterMap != null) {
                fileParameterMap.remove(name);
            }
        }
    }

    public void setFileParameter(String name, FileParameter fileParameter) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchFileParameterMap().setFileParameter(name, fileParameter);
        }
    }

    public void setFileParameter(String name, FileParameter[] fileParameters) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            touchFileParameterMap().setFileParameter(name, fileParameters);
        }
    }

    public Enumeration<String> getFileParameterNames() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            FileParameterMap fileParameterMap = touchFileParameterMap();
            return (fileParameterMap != null ? Collections.enumeration(fileParameterMap.keySet()) : null);
        }
    }

    private FileParameterMap touchFileParameterMap() {
        if (fileParameterMap == null) {
            fileParameterMap = new FileParameterMap();
        }
        return fileParameterMap;
    }

    public abstract <T> T getAttribute(String name);

    public abstract Enumeration<String> getAttributeNames();

    public abstract void setAttribute(String name, Object value);

    public abstract void removeAttribute(String name);

    /**
     * Returns an unmodifiable map of the attributes.
     *
     * @return an unmodifiable map of the attributes
     */
    public abstract Map<String, Object> getAllAttributes();

    /**
     * Copies all of the mappings from the specified attributes.
     *
     * @param attributes the specified attributes
     */
    public abstract void putAllAttributes(Map<String, Object> attributes);

    public abstract void fillAllAttributes(Map<String, Object> targetAttributes);

    /**
     * Sets whether the request header has exceeded the maximum length.
     *
     * @param maxLengthExceeded whether the request header has exceeded the maximum length
     */
    public void setMaxLengthExceeded(boolean maxLengthExceeded) {
        this.maxLengthExceeded = maxLengthExceeded;
    }

    /**
     * Returns whether request header has exceed the maximum length.
     *
     * @return true, if is max length exceeded
     */
    public boolean isMaxLengthExceeded() {
        return maxLengthExceeded;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Gets the time zone.
     *
     * @return the time zone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone.
     *
     * @param timeZone the time zone
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

}
