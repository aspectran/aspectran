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
package com.aspectran.core.adapter;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.component.bean.scope.RequestScope;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.apon.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Provides an abstraction for an incoming request within a specific runtime environment.
 *
 * <p>Implementations of this interface encapsulate a container-specific request object
 * (e.g., {@code HttpServletRequest} in a web environment), exposing a consistent API
 * for accessing request data such as headers, parameters, attributes, and uploaded files.
 * This allows request handling logic to remain uniform across different execution contexts.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface RequestAdapter {

    /**
     * Returns the underlying native request object that this adapter wraps.
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Checks if a request scope has been created for this request.
     * @return true if the request scope exists, false otherwise
     */
    boolean hasRequestScope();

    /**
     * Returns the request scope associated with this request.
     * If a scope does not already exist, a new one is created and associated.
     * @return the request scope
     */
    RequestScope getRequestScope();

    /**
     * Returns the value of the specified request header.
     * If the header has multiple values, the first value is returned.
     * @param name the name of the header
     * @return the header value, or {@code null} if the header is not found
     */
    String getHeader(String name);

    /**
     * Returns all values of the specified request header.
     * @param name the name of the header
     * @return a list of header values, or {@code null} if the header is not found
     */
    List<String> getHeaderValues(String name);

    /**
     * Returns a set of all header names in this request.
     * @return a set of header names, which may be empty
     */
    Set<String> getHeaderNames();

    /**
     * Checks if the specified request header exists.
     * @param name the name of the header
     * @return true if the header exists, false otherwise
     */
    boolean containsHeader(String name);

    /**
     * Sets a header, overwriting any existing value.
     * @param name the name of the header
     * @param value the header value
     */
    void setHeader(String name, String value);

    /**
     * Adds a header value to the specified header.
     * If the header already exists, the new value is added to the list of existing values.
     * @param name the name of the header
     * @param value the header value to add
     */
    void addHeader(String name, String value);

    /**
     * Returns a mutable map of all request headers.
     * @return a {@link MultiValueMap} of headers
     */
    MultiValueMap<String, String> getHeaderMap();

    /**
     * Checks if this request has any headers.
     * @return true if headers exist, false otherwise
     */
    boolean hasHeaders();

    /**
     * Returns the value of the specified request-scoped attribute.
     * @param <T> the type of the attribute
     * @param name the name of the attribute
     * @return the attribute value, or {@code null} if it does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Sets a request-scoped attribute.
     * @param name the name of the attribute
     * @param value the attribute value to set
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a set of all attribute names in this request.
     * @return a set of attribute names, which may be empty
     */
    Set<String> getAttributeNames();

    /**
     * Removes the specified request-scoped attribute.
     * @param name the name of the attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Copies all mappings from the given map to this request's attributes.
     * @param attributes the map of attributes to copy
     */
    void putAllAttributes(Map<String, Object> attributes);

    /**
     * Extracts all attributes from this request and populates the given map.
     * @param targetAttributes the map to populate with attributes
     */
    void extractAttributes(Map<String, Object> targetAttributes);

    /**
     * Returns a mutable map of all request-scoped attributes.
     * @return a map of attributes
     */
    Map<String, Object> getAttributeMap();

    /**
     * Checks if this request has any attributes.
     * @return true if attributes exist, false otherwise
     */
    boolean hasAttributes();

    /**
     * Checks if the specified request-scoped attribute exists.
     * @param name the name of the attribute
     * @return true if the attribute exists, false otherwise
     */
    boolean hasAttribute(String name);

    /**
     * Returns the value of a request parameter.
     * If the parameter has multiple values, the first value is returned.
     * @param name the name of the parameter
     * @return the parameter value, or {@code null} if it does not exist
     * @see #getParameterValues
     */
    String getParameter(String name);

    /**
     * Returns all values of a request parameter.
     * @param name the name of the parameter
     * @return an array of parameter values, or {@code null} if the parameter does not exist
     * @see #getParameter
     */
    String[] getParameterValues(String name);

    /**
     * Returns a collection of all parameter names in this request.
     * @return a collection of parameter names, which may be empty
     */
    Collection<String> getParameterNames();

    /**
     * Sets a parameter value, overwriting any existing value.
     * @param name the name of the parameter
     * @param value the parameter value
     * @see #setParameter(String, String[])
     */
    void setParameter(String name, String value);

    /**
     * Sets a parameter with multiple values.
     * @param name the name of the parameter
     * @param values an array of parameter values
     * @see #setParameter(String, String)
     */
    void setParameter(String name, String[] values);

    /**
     * Returns a map of all request parameters.
     * The map values can be of type String or String[].
     * @return a map of all parameters
     */
    Map<String, Object> getAllParameters();

    /**
     * Copies all mappings from the given {@link ParameterMap} to this request's parameters.
     * @param parameterMap the parameters to copy
     * @since 5.2.3
     */
    void putAllParameters(ParameterMap parameterMap);

    /**
     * Copies all mappings from the given {@link MultiValueMap} to this request's parameters.
     * @param multiValueMap the parameters to copy
     * @since 6.1.2
     */
    void putAllParameters(MultiValueMap<String, String> multiValueMap);

    /**
     * Extracts all parameters from this request and populates the given map.
     * @param targetParameters the map to populate with parameters
     */
    void extractParameters(Map<String, Object> targetParameters);

    /**
     * Returns a mutable {@link ParameterMap} of all request parameters.
     * @return the parameter map
     * @since 1.4.0
     */
    ParameterMap getParameterMap();

    /**
     * Checks if this request has any parameters.
     * @return true if parameters exist, false otherwise
     */
    boolean hasParameters();

    /**
     * Checks if the specified parameter exists.
     * @param name the name of the parameter
     * @return true if the parameter exists, false otherwise
     */
    boolean hasParameter(String name);

    /**
     * Returns the uploaded file for the specified parameter name.
     * If there are multiple files with the same name, the first one is returned.
     * @param name the name of the file parameter
     * @return the {@link FileParameter} object, or {@code null} if it does not exist
     * @see #getFileParameterValues
     */
    FileParameter getFileParameter(String name);

    /**
     * Returns all uploaded files for the specified parameter name.
     * @param name the name of the file parameter
     * @return an array of {@link FileParameter} objects, or {@code null} if none exist
     * @see #getFileParameter
     */
    FileParameter[] getFileParameterValues(String name);

    /**
     * Returns a set of all file parameter names in this request.
     * @return a set of file parameter names, which may be empty
     */
    Set<String> getFileParameterNames();

    /**
     * Sets a file parameter.
     * @param name the name of the file parameter
     * @param fileParameter the {@link FileParameter} object
     * @see #setFileParameter(String, FileParameter[])
     */
    void setFileParameter(String name, FileParameter fileParameter);

    /**
     * Sets a file parameter with multiple files.
     * @param name the name of the file parameter
     * @param fileParameters an array of {@link FileParameter} objects
     * @see #setFileParameter(String, FileParameter)
     */
    void setFileParameter(String name, FileParameter[] fileParameters);

    /**
     * Removes the specified file parameter.
     * @param name the name of the file parameter to remove
     */
    void removeFileParameter(String name);

    /**
     * Copies all mappings from the given map to this request's file parameters.
     * @param fileParameterMap the map of file parameters to copy
     * @since 6.1.2
     */
    void putAllFileParameters(MultiValueMap<String, FileParameter> fileParameterMap);

    /**
     * Checks if this request has any file parameters.
     * @return true if file parameters exist, false otherwise
     */
    boolean hasFileParameters();

    /**
     * Returns the request method (e.g., GET, POST).
     * @return the {@link MethodType}
     */
    MethodType getRequestMethod();

    /**
     * Returns the character encoding of the request body.
     * @return the character encoding name, or {@code null} if not specified
     */
    String getEncoding();

    /**
     * Sets the character encoding of the request body.
     * This should be called before reading parameters or the request body.
     * @param encoding the character encoding name
     * @throws UnsupportedEncodingException if the specified encoding is not supported
     */
    void setEncoding(String encoding) throws UnsupportedEncodingException;

    /**
     * Returns the preferred locale of the client.
     * @return the preferred {@link Locale}
     */
    Locale getLocale();

    /**
     * Sets the locale for this request.
     * @param locale the {@link Locale} to set
     */
    void setLocale(Locale locale);

    /**
     * Returns the preferred time zone of the client.
     * @return the preferred {@link TimeZone}
     */
    TimeZone getTimeZone();

    /**
     * Sets the time zone for this request.
     * @param timeZone the {@link TimeZone} to set
     */
    void setTimeZone(TimeZone timeZone);

    /**
     * Gets the maximum size allowed for the request body.
     * @return the maximum request size in bytes
     */
    long getMaxRequestSize();

    /**
     * Sets the maximum size allowed for the request body.
     * @param maxRequestSize the maximum request size in bytes
     */
    void setMaxRequestSize(long maxRequestSize);

    /**
     * Returns an input stream for reading the request body.
     * @return the {@link InputStream}
     * @throws IOException if an I/O error occurs
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the request body as a string.
     * @return the request body, or {@code null} if not available
     */
    String getBody();

    /**
     * Sets the request body as a string.
     * @param body the request body string
     */
    void setBody(String body);

    /**
     * Parses the request body into a {@link Parameters} object.
     * @return the parsed parameters
     * @throws RequestParseException if an error occurs during parsing
     */
    Parameters getBodyAsParameters() throws RequestParseException;

    /**
     * Parses the request body into an object of the specified {@link Parameters} type.
     * @param <T> the type of the parameters object
     * @param requiredType the class of the parameters object to return
     * @return the parsed parameters object
     * @throws RequestParseException if an error occurs during parsing
     */
    <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) throws RequestParseException;

    /**
     * Returns all request parameters (from query string and body) as a {@link Parameters} object.
     * @return the parameters object
     */
    Parameters getParameters();

    /**
     * Returns all request parameters (from query string and body) as an object of the specified type.
     * @param <T> the type of the parameters object
     * @param requiredType the class of the parameters object to return
     * @return the parameters object
     */
    <T extends Parameters> T getParameters(Class<T> requiredType);

    /**
     * Returns the {@link Principal} object representing the authenticated user.
     * @return the user principal, or {@code null} if the user is not authenticated
     */
    Principal getPrincipal();

}
