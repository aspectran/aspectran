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

    private final boolean touchFirst;

    private MethodType requestMethod;

    private MultiValueMap<String, String> headers;

    private ParameterMap parameterMap;

    private FileParameterMap fileParameterMap;

    private Map<String, Object> attributes;

    private String encoding;

    private Locale locale;

    private TimeZone timeZone;

    private boolean maxLengthExceeded;

    public AbstractRequest(boolean touchFirst) {
        this.touchFirst = touchFirst;
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
        if (touchFirst) {
            return touchHeaders().getFirst(name);
        } else {
            return (headers != null ? headers.getFirst(name) : null);
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
        if (touchFirst) {
            return touchHeaders().get(name);
        } else {
            return (headers != null ? headers.get(name) : null);
        }
    }

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    public Collection<String> getHeaderNames() {
        if (touchFirst) {
            return touchHeaders().keySet();
        } else {
            return (headers != null ? headers.keySet() : null);
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
        if (touchFirst) {
            List<String> values = touchHeaders().get(name);
            return (values != null && !values.isEmpty());
        } else {
            if (headers != null) {
                List<String> values = headers.get(name);
                return (values != null && !values.isEmpty());
            } else {
                return false;
            }
        }
    }

    /**
     * Set the given single header value under the given header name.
     *
     * @param name the header name
     * @param value the header value to set
     */
    public void setHeader(String name, String value) {
        touchHeaders().set(name, value);
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    public void addHeader(String name, String value) {
        touchHeaders().add(name, value);
    }

    /**
     * Returns a map of the request headers that can be modified.
     *
     * @return an {@code MultiValueMap} object, must not be {@code null}
     */
    public MultiValueMap<String, String> getAllHeaders() {
        return touchHeaders();
    }

    /**
     * Returns a map of the request headers that can be modified.
     * If not yet instantiated then create a new one.
     * Must be touch first, because it is lazy initialization.
     *
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
    public MultiValueMap<String, String> touchHeaders() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        return headers;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public String getParameter(String name) {
        if (touchFirst) {
            return touchParameterMap().getParameter(name);
        } else {
            return (parameterMap != null ? parameterMap.getParameter(name) : null);
        }
    }

    public String[] getParameterValues(String name) {
        if (touchFirst) {
            return touchParameterMap().getParameterValues(name);
        } else {
            return (parameterMap != null ? parameterMap.getParameterValues(name) : null);
        }
    }

    public Collection<String> getParameterNames() {
        if (touchFirst) {
            return touchParameterMap().getParameterNames();
        } else {
            return (parameterMap != null ? parameterMap.getParameterNames() : null);
        }
    }

    public void setParameter(String name, String value) {
        touchParameterMap().setParameter(name, value);
    }

    public void setParameter(String name, String[] values) {
        touchParameterMap().put(name, values);
    }

    /**
     * Returns a map of the request parameters that can be modified.
     *
     * @return an {@code Map<String, Object>} object, must not be {@code null}
     */
    public Map<String, Object> getAllParameters() {
        return touchParameterMap().extractParameters();
    }

    public void putAllParameters(ParameterMap parameterMap) {
        touchParameterMap().putAll(parameterMap);
    }

    public void extractParameters(Map<String, Object> targetParameters) {
        if (touchFirst) {
            touchParameterMap().extractParameters(targetParameters);
        } else {
            if (parameterMap != null) {
                parameterMap.extractParameters(targetParameters);
            }
        }
    }

    /**
     * Must be touch first, because it is lazy initialization.
     * @return the parameter map
     */
    public ParameterMap touchParameterMap() {
        if (parameterMap == null) {
            parameterMap = new ParameterMap();
        }
        return parameterMap;
    }

    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public FileParameter getFileParameter(String name) {
        if (touchFirst) {
            return touchFileParameterMap().getFileParameter(name);
        } else {
            return (fileParameterMap != null ? fileParameterMap.getFileParameter(name) : null);
        }
    }

    public FileParameter[] getFileParameterValues(String name) {
        if (touchFirst) {
            return touchFileParameterMap().getFileParameterValues(name);
        } else {
            return (fileParameterMap != null ? fileParameterMap.getFileParameterValues(name) : null);
        }
    }

    public void removeFileParameter(String name) {
        if (touchFirst) {
            touchFileParameterMap().remove(name);
        } else {
            if (fileParameterMap != null) {
                fileParameterMap.remove(name);
            }
        }
    }

    public void setFileParameter(String name, FileParameter fileParameter) {
        touchFileParameterMap().setFileParameter(name, fileParameter);
    }

    public void setFileParameter(String name, FileParameter[] fileParameters) {
        touchFileParameterMap().setFileParameter(name, fileParameters);
    }

    public Collection<String> getFileParameterNames() {
        if (touchFirst) {
            return touchFileParameterMap().keySet();
        } else {
            return (fileParameterMap != null ? fileParameterMap.keySet() : null);
        }
    }

    /**
     * Must be touch first, because it is lazy initialization.
     *
     * @return the file parameter map
     */
    public FileParameterMap touchFileParameterMap() {
        if (fileParameterMap == null) {
            fileParameterMap = new FileParameterMap();
        }
        return fileParameterMap;
    }

    public FileParameterMap getFileParameterMap() {
        return fileParameterMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        if (touchFirst) {
            return (T) touchAttributes().get(name);
        } else {
            return (attributes != null ? (T)attributes.get(name) : null);
        }
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            // If the object passed in is null, the effect is the same as calling removeAttribute(java.lang.String).
            removeAttribute(name);
        } else {
            touchAttributes().put(name, value);
        }
    }

    public Collection<String> getAttributeNames() {
        if (touchFirst) {
            return touchAttributes().keySet();
        } else {
            return (attributes != null ? attributes.keySet() : null);
        }
    }

    public void removeAttribute(String name) {
        if (touchFirst) {
            touchAttributes().remove(name);
        } else {
            if (attributes != null) {
                attributes.remove(name);
            }
        }
    }

    public Map<String, Object> getAllAttributes() {
        return touchAttributes();
    }

    public void putAllAttributes(Map<String, Object> attributes) {
        touchAttributes().putAll(attributes);
    }

    public void extractAttributes(Map<String, Object> targetAttributes) {
        if (targetAttributes == null) {
            throw new IllegalArgumentException("Argument 'targetAttributes' must not be null");
        }
        if (touchFirst) {
            targetAttributes.putAll(touchAttributes());
        } else {
            if (attributes != null) {
                targetAttributes.putAll(attributes);
            }
        }
    }

    /**
     * Must be touch first, because it is lazy initialization.
     *
     * @return the attribute map
     */
    public Map<String, Object> touchAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public Map<String, Object> getAttributeMap() {
        return attributes;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributes = attributeMap;
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
