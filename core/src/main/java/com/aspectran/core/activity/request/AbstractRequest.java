/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.core.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
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

    private Map<String, Object> attributes;

    private String encoding;

    private Locale locale;

    private TimeZone timeZone;

    private boolean maxLengthExceeded;

    public AbstractRequest() {
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
        return getHeaderMap().getFirst(name);
    }

    /**
     * Returns the values of the response header with the given name.
     *
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    public Collection<String> getHeaders(String name) {
        return getHeaderMap().get(name);
    }

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    public Collection<String> getHeaderNames() {
        return getHeaderMap().keySet();
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
        List<String> values = getHeaderMap().get(name);
        return (values != null && !values.isEmpty());
    }

    /**
     * Set the given single header value under the given header name.
     *
     * @param name the header name
     * @param value the header value to set
     */
    public void setHeader(String name, String value) {
        getHeaderMap().set(name, value);
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    public void addHeader(String name, String value) {
        getHeaderMap().add(name, value);
    }

    /**
     * Returns a map of the request headers that can be modified.
     * If not yet instantiated then create a new one.
     *
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
    public MultiValueMap<String, String> getHeaderMap() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        return headers;
    }

    public void setHeaderMap(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public boolean hasHeaders() {
        return (headers != null && !headers.isEmpty());
    }

    public String getParameter(String name) {
        return getParameterMap().getParameter(name);
    }

    public String[] getParameterValues(String name) {
        return getParameterMap().getParameterValues(name);
    }

    public Collection<String> getParameterNames() {
        return getParameterMap().getParameterNames();
    }

    public void setParameter(String name, String value) {
        getParameterMap().setParameter(name, value);
    }

    public void setParameter(String name, String[] values) {
        getParameterMap().put(name, values);
    }

    /**
     * Returns a map of the request parameters that can be modified.
     *
     * @return an {@code Map<String, Object>} object, must not be {@code null}
     */
    public Map<String, Object> getAllParameters() {
        return getParameterMap().extractAsMap();
    }

    public void putAllParameters(ParameterMap parameterMap) {
        getParameterMap().putAll(parameterMap);
    }

    public void putAllParameters(MultiValueMap<String, String> parameterMap) {
        if (parameterMap != null && !parameterMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
                String name = entry.getKey();
                List<String> list = entry.getValue();
                String[] values = list.toArray(new String[0]);
                getParameterMap().setParameterValues(name, values);
            }
        }
    }

    public void extractParameters(Map<String, Object> targetMap) {
        if (hasParameters()) {
            getParameterMap().extractAsMap(targetMap);
        }
    }

    /**
     * Returns a map of the request parameters that can be modified.
     * If not yet instantiated then create a new one.
     *
     * @return an {@code ParameterMap} object, may not be {@code null}
     */
    public ParameterMap getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new ParameterMap();
        }
        return parameterMap;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public boolean hasParameters() {
        return (parameterMap != null && !parameterMap.isEmpty());
    }

    public FileParameter getFileParameter(String name) {
        return getFileParameterMap().getFileParameter(name);
    }

    public FileParameter[] getFileParameterValues(String name) {
        return getFileParameterMap().getFileParameterValues(name);
    }

    public Collection<String> getFileParameterNames() {
        return getFileParameterMap().keySet();
    }

    public void setFileParameter(String name, FileParameter fileParameter) {
        getFileParameterMap().setFileParameter(name, fileParameter);
    }

    public void setFileParameter(String name, FileParameter[] fileParameters) {
        getFileParameterMap().setFileParameterValues(name, fileParameters);
    }

    public void removeFileParameter(String name) {
        getFileParameterMap().remove(name);
    }

    public FileParameterMap getFileParameterMap() {
        if (fileParameterMap == null) {
            fileParameterMap = new FileParameterMap();
        }
        return fileParameterMap;
    }

    public void putAllFileParameters(MultiValueMap<String, FileParameter> fileParameterMap) {
        if (fileParameterMap != null && !fileParameterMap.isEmpty()) {
            for (Map.Entry<String, List<FileParameter>> entry : fileParameterMap.entrySet()) {
                String name = entry.getKey();
                List<FileParameter> list = entry.getValue();
                FileParameter[] values = list.toArray(new FileParameter[0]);
                getFileParameterMap().setFileParameterValues(name, values);
            }
        }
    }

    public boolean hasFileParameters() {
        return (fileParameterMap != null && !fileParameterMap.isEmpty());
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)getAttributeMap().get(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            // If the object passed in is null, the effect is the same
            // as calling removeAttribute(java.lang.String).
            removeAttribute(name);
        } else {
            getAttributeMap().put(name, value);
        }
    }

    public Collection<String> getAttributeNames() {
        return getAttributeMap().keySet();
    }

    public void removeAttribute(String name) {
        getAttributeMap().remove(name);
    }

    public void putAllAttributes(Map<String, Object> attributes) {
        getAttributeMap().putAll(attributes);
    }

    public void extractAttributes(Map<String, Object> targetMap) {
        if (targetMap == null) {
            throw new IllegalArgumentException("targetMap must not be null");
        }
        if (hasAttributes()) {
            targetMap.putAll(getAttributeMap());
        }
    }

    /**
     * Returns a map of the request attributes that can be modified.
     * If not yet instantiated then create a new one.
     *
     * @return an {@code Map<String, Object>} object, may not be {@code null}
     */
    public Map<String, Object> getAttributeMap() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributes = attributeMap;
    }

    public boolean hasAttributes() {
        return (attributes != null && !attributes.isEmpty());
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        this.encoding = encoding;
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

}
