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
package com.aspectran.core.activity.request;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * An abstract base class for request implementations within the Aspectran framework.
 * <p>
 * Provides common functionality for handling request parameters, headers, body content,
 * and path variables. Concrete subclasses typically adapt input from specific sources
 * such as HTTP, CLI, or daemon processes. This class serves as a foundation to reduce
 * duplication and enforce a consistent request handling model.
 * </p>
 *
 * <p>Responsibilities may include:</p>
 * <ul>
 *   <li>Managing parameter maps and path variable maps</li>
 *   <li>Parsing and exposing the request body via a {@link RequestBodyParser}</li>
 *   <li>Handling file upload parameters</li>
 *   <li>Providing access to metadata such as request content type</li>
 * </ul>
 *
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

    private final MethodType requestMethod;

    private MultiValueMap<String, String> headers;

    private Map<String, Object> attributes;

    private ParameterMap parameterMap;

    private FileParameterMap fileParameterMap;

    private String encoding;

    private Locale locale;

    private TimeZone timeZone;

    private long maxRequestSize;

    private String body;

    /**
     * Creates a new request with the specified method type.
     * @param requestMethod the request method (e.g., GET, POST)
     */
    public AbstractRequest(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /** @return the request method used (e.g., GET, POST). */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Returns the value of the request header with the given name.
     * <p>If a request header with the given name exists and contains
     * multiple values, the value that was added first will be returned.</p>
     * @param name the name of the request header whose value to return
     * @return the value of the request header with the given name,
     *         or {@code null} if no header with the given name has been set
     *         on this request
     */
    public String getHeader(String name) {
        return getHeaderMap().getFirst(name);
    }

    /**
     * Returns the values of the request header with the given name.
     * @param name the name of the request header whose values to return
     * @return a (possibly empty) {@code List} of the values
     *         of the request header with the given name
     */
    public List<String> getHeaderValues(String name) {
        return getHeaderMap().get(name);
    }

    /**
     * Returns the names of the headers of this request.
     * @return a (possibly empty) {@code Set} of the names
     *         of the headers of this request
     */
    public Set<String> getHeaderNames() {
        return getHeaderMap().keySet();
    }

    /**
     * Returns a boolean indicating whether the named request header
     * has already been set.
     * @param name the header name
     * @return {@code true} if the named request header
     *         has already been set; {@code false} otherwise
     */
    public boolean containsHeader(String name) {
        List<String> values = getHeaderMap().get(name);
        return (values != null && !values.isEmpty());
    }

    /**
     * Set the given single header value under the given header name.
     * @param name the header name
     * @param value the header value to set
     */
    public void setHeader(String name, String value) {
        getHeaderMap().set(name, value);
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     * @param name the header name
     * @param value the header value to be added
     */
    public void addHeader(String name, String value) {
        getHeaderMap().add(name, value);
    }

    /**
     * Returns a map of the request headers that can be modified.
     * If not yet instantiated then create a new one.
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
    public MultiValueMap<String, String> getHeaderMap() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        return headers;
    }

    /**
     * Sets the header map for this request.
     */
    public void setHeaderMap(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @return whether this request has any headers.
     */
    public boolean hasHeaders() {
        return (headers != null && !headers.isEmpty());
    }

    /**
     * Retrieves an attribute by name.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)getAttributeMap().get(name);
    }

    /**
     * Sets an attribute by name.
     * <p>If the value is {@code null}, it has the same effect as calling
     * {@link #removeAttribute(String)}.</p>
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            // If the object passed in is null, the effect is the same
            // as calling removeAttribute(java.lang.String).
            removeAttribute(name);
        } else {
            getAttributeMap().put(name, value);
        }
    }

    /**
     * @return the names of all attributes associated with this request.
     */
    public Set<String> getAttributeNames() {
        return getAttributeMap().keySet();
    }

    /**
     * Removes the attribute with the given name.
     */
    public void removeAttribute(String name) {
        getAttributeMap().remove(name);
    }

    /**
     * Adds all attributes from the provided map.
     */
    public void putAllAttributes(Map<String, Object> attributes) {
        getAttributeMap().putAll(attributes);
    }

    /**
     * Extracts attributes into the provided target map.
     */
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
     * @return an {@code Map<String, Object>} object, may not be {@code null}
     */
    public Map<String, Object> getAttributeMap() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    /**
     * Replaces the attribute map for this request.
     */
    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributes = attributeMap;
    }

    /**
     * @return whether any attributes exist.
     */
    public boolean hasAttributes() {
        return (attributes != null && !attributes.isEmpty());
    }

    /**
     * @return whether the given attribute exists.
     */
    public boolean hasAttribute(String name) {
        if (attributes != null) {
            return attributes.containsKey(name);
        } else {
            return false;
        }
    }

    /**
     * Retrieves a single parameter value.
     */
    public String getParameter(String name) {
        return getParameterMap().getParameter(name);
    }

    /**
     * Retrieves multiple values for a parameter.
     */
    public String[] getParameterValues(String name) {
        return getParameterMap().getParameterValues(name);
    }

    /**
     * @return the names of all available parameters.
     */
    public Set<String> getParameterNames() {
        return getParameterMap().getParameterNames();
    }

    /**
     * Sets a single parameter value.
     */
    public void setParameter(String name, String value) {
        getParameterMap().setParameter(name, value);
    }

    /**
     * Sets multiple values for a parameter.
     */
    public void setParameter(String name, String[] values) {
        getParameterMap().put(name, values);
    }

    /**
     * Returns all parameters as a mutable {@code Map<String, Object>}.
     * @return an {@code Map<String, Object>} object, must not be {@code null}
     */
    public Map<String, Object> getAllParameters() {
        return getParameterMap().extractAsMap();
    }

    /**
     * Adds parameters from a {@link ParameterMap}.
     */
    public void putAllParameters(ParameterMap parameterMap) {
        getParameterMap().putAll(parameterMap);
    }

    /**
     * Adds parameters from a {@link MultiValueMap}.
     */
    public void putAllParameters(MultiValueMap<String, String> multiValueMap) {
        if (multiValueMap != null && !multiValueMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : multiValueMap.entrySet()) {
                String name = entry.getKey();
                List<String> list = entry.getValue();
                String[] values = list.toArray(new String[0]);
                getParameterMap().setParameterValues(name, values);
            }
        }
    }

    /**
     * Extracts parameters into the provided target map.
     */
    public void extractParameters(Map<String, Object> targetMap) {
        if (hasParameters()) {
            getParameterMap().extractAsMap(targetMap);
        }
    }

    /**
     * Returns a map of the request parameters that can be modified.
     * If not yet instantiated then create a new one.
     * @return a {@code ParameterMap} object, may not be {@code null}
     */
    public ParameterMap getParameterMap() {
        if (parameterMap == null) {
            parameterMap = new ParameterMap();
        }
        return parameterMap;
    }

    /**
     * Replaces the parameter map for this request.
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * @return whether any parameters exist.
     */
    public boolean hasParameters() {
        return (parameterMap != null && !parameterMap.isEmpty());
    }

    /**
     * @return whether a parameter with the given name exists.
     */
    public boolean hasParameter(String name) {
        if (parameterMap != null) {
            return parameterMap.containsKey(name);
        } else {
            return false;
        }
    }

    /**
     * Retrieves the first uploaded file for the given name.
     */
    public FileParameter getFileParameter(String name) {
        return getFileParameterMap().getFileParameter(name);
    }

    /**
     * Retrieves all uploaded files for the given name.
     */
    public FileParameter[] getFileParameterValues(String name) {
        return getFileParameterMap().getFileParameterValues(name);
    }

    /**
     * @return the names of all file parameters.
     */
    public Set<String> getFileParameterNames() {
        return getFileParameterMap().keySet();
    }

    /**
     * Associates a single file parameter with the given name.
     */
    public void setFileParameter(String name, FileParameter fileParameter) {
        getFileParameterMap().setFileParameter(name, fileParameter);
    }

    /**
     * Associates multiple file parameters with the given name.
     */
    public void setFileParameter(String name, FileParameter[] fileParameters) {
        getFileParameterMap().setFileParameterValues(name, fileParameters);
    }

    /**
     * Removes all file parameters under the given name.
     */
    public void removeFileParameter(String name) {
        getFileParameterMap().remove(name);
    }

    /**
     * @return the file parameter map.
     */
    public FileParameterMap getFileParameterMap() {
        if (fileParameterMap == null) {
            fileParameterMap = new FileParameterMap();
        }
        return fileParameterMap;
    }

    /**
     * Adds all file parameters from the provided map.
     */
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

    /**
     * @return whether any file parameters exist.
     */
    public boolean hasFileParameters() {
        return (fileParameterMap != null && !fileParameterMap.isEmpty());
    }

    /**
     * @return the character encoding of the request body.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for the request body.
     */
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        this.encoding = encoding;
    }

    /**
     * @return the locale of this request.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale of this request.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return the time zone associated with this request.
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone for this request.
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the maximum request size in bytes.
     */
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * Sets the maximum request size in bytes.
     */
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    /**
     * @return an input stream for reading the request body
     * @throws IOException if the stream cannot be opened
     */
    public InputStream getInputStream() throws IOException {
        if (getBody() != null) {
            try {
                String enc = (getEncoding() != null ? getEncoding() : ActivityContext.DEFAULT_ENCODING);
                return new ByteArrayInputStream(getBody().getBytes(enc));
            } catch (UnsupportedEncodingException e) {
                throw new IOException("Unsupported encoding: " + e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    /**
     * @return the request body as a string.
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the request body content.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Parses the body into a {@link Parameters} object.
     * @return parsed parameters
     * @throws RequestParseException if parsing fails
     */
    public Parameters getBodyAsParameters() throws RequestParseException {
        return getBodyAsParameters(VariableParameters.class);
    }

    /**
     * Parses the body into the specified type of {@link Parameters}.
     * @param requiredType the parameter type to return
     * @param <T> a subtype of {@link Parameters}
     * @return parsed parameters of the given type
     * @throws RequestParseException if parsing fails
     */
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) throws RequestParseException {
        return RequestBodyParser.parseBodyAsParameters(getBody(), requiredType);
    }

    /**
     * @return all request parameters as a {@link Parameters} object.
     */
    public Parameters getParameters() {
        return getParameters(VariableParameters.class);
    }

    /**
     * Returns request parameters as a specific subclass of {@link Parameters}.
     * @param requiredType the parameter type to return
     * @param <T> a subtype of {@link Parameters}
     * @return parsed parameters of the given type
     */
    public <T extends Parameters> T getParameters(Class<T> requiredType) {
        T parameters = ClassUtils.createInstance(requiredType);
        for (Map.Entry<String, String[]> entry : getParameterMap().entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            for (String value : values) {
                parameters.putValue(name, value);
            }
        }
        return parameters;
    }

}
