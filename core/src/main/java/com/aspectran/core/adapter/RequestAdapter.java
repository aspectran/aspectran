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
package com.aspectran.core.adapter;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.component.bean.scope.RequestScope;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * The Interface RequestAdapter.
 *
 * @since 2011. 3. 13.
 */
public interface RequestAdapter {

    /**
     * Returns the adaptee object to provide request information.
     *
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Gets the request scope.
     *
     * @return the request scope
     */
    RequestScope getRequestScope();

    /**
     * Gets the request scope.
     *
     * @param create {@code true} to create a new request scope for this
     *         request if necessary; {@code false} to return {@code null}
     * @return the request scope
     */
    RequestScope getRequestScope(boolean create);

    /**
     * Returns the method used for the request.
     *
     * @return a {@code MethodType} object
     */
    MethodType getRequestMethod();

    /**
     * Returns a map of the request headers that can be modified.
     *
     * @return an {@code MultiValueMap} object, may be {@code null}
     */
    MultiValueMap<String, String> getAllHeaders();

    /**
     * Returns the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     *
     * @param name the name of the response header whose value to return
     * @return the value of the response header with the given name,
     *         or {@code null} if no header with the given name has been set
     *         on this response
     */
    String getHeader(String name);

    /**
     * Returns the values of the response header with the given name.
     *
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    Collection<String> getHeaders(String name);

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    Collection<String> getHeaderNames();

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     *
     * @param name the header name
     * @return {@code true} if the named response header
     *         has already been set; {@code false} otherwise
     */
    boolean containsHeader(String name);

    /**
     * Set the given single header value under the given header name.
     *
     * @param name the header name
     * @param value the header value to set
     */
    void setHeader(String name, String value);

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    void addHeader(String name, String value);

    /**
     * Returns the value of a request parameter as a {@code String},
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return a {@code String} representing the
     *         single value of the parameter
     * @see #getParameterValues
     */
    String getParameter(String name);

    /**
     * Returns an array of {@code String} objects containing all
     * of the values the given activity's request parameter has,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @return an array of {@code String} objects
     *         containing the parameter's values
     * @see #getParameter
     */
    String[] getParameterValues(String name);

    /**
     * Returns a {@code Collection} of {@code String} objects containing
     * the names of the parameters contained in this request.
     * If the request has no parameters, the method returns an empty {@code Enumeration}.
     *
     * @return a {@code Collection} of {@code String} objects, each {@code String}
     *         containing the name of a request parameter;
     *         or an empty {@code Enumeration} if the request has no parameters
     */
    Collection<String> getParameterNames();

    /**
     * Sets the value to the parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param value a {@code String} representing the
     *         single value of the parameter
     * @see #setParameter(String, String[])
     */
    void setParameter(String name, String value);

    /**
     * Sets the value to the parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param values an array of {@code String} objects
     *         containing the parameter's values
     * @see #setParameter
     */
    void setParameter(String name, String[] values);

    /**
     * Return an mutable Map of the request parameters,
     * with parameter names as map keys and parameter values as map values.
     * If the parameter value type is the {@code String} then map value will be of type {@code String}.
     * If the parameter value type is the {@code String} array then map value will be of type {@code String} array.
     *
     * @return the mutable parameter map
     * @since 1.4.0
     */
    Map<String, Object> getAllParameters();

    /**
     * Copies all of the mappings from the specified parameters.
     *
     * @param parameterMap the specified parameters
     * @since 5.2.3
     */
    void putAllParameters(ParameterMap parameterMap);

    /**
     * Extracts all the parameters and fills in the specified map.
     *
     * @param targetParameters the target parameter map to be filled
     * @since 2.0.0
     */
    void extractParameters(Map<String, Object> targetParameters);

    /**
     * Returns a {@code FileParameter} object as a given request parameter name,
     * or {@code null} if the file parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @return a {@code FileParameter} representing the
     *         single value of the parameter
     * @see #getFileParameterValues
     */
    FileParameter getFileParameter(String name);

    /**
     * Returns an array of {@code FileParameter} objects containing all
     * of the values the given request parameter has,
     * or {@code null} if the parameter does not exist.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @return an array of {@code FileParameter} objects
     *         containing the parameter's values
     * @see #getFileParameter
     */
    FileParameter[] getFileParameterValues(String name);

    /**
     * Returns a {@code Collection} of {@code String} objects containing
     * the names of the file parameters contained in this request.
     * If the request has no parameters, the method returns an empty {@code Collection}.
     *
     * @return a {@code Collection} of {@code String} objects, each {@code String}
     *         containing the name of a file parameter;
     *         or an empty {@code Collection} if the request has no file parameters
     */
    Collection<String> getFileParameterNames();

    /**
     * Sets the {@code FileParameter} object to the file parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameter a {@code FileParameter} representing the
     *         single value of the parameter
     * @see #setFileParameter(String, FileParameter[])
     */
    void setFileParameter(String name, FileParameter fileParameter);

    /**
     * Sets the value to the file parameter with the given name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     * @param fileParameters an array of {@code FileParameter} objects
     *         containing the file parameter's values
     * @see #setFileParameter
     */
    void setFileParameter(String name, FileParameter[] fileParameters);

    /**
     * Removes the file parameter with the specified name.
     *
     * @param name a {@code String} specifying the name of the file parameter
     */
    void removeFileParameter(String name);

    /**
     * Returns the value of the named attribute as a given type,
     * or {@code null} if no attribute of the given name exists.
     *
     * @param <T> the generic type
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *         or {@code null} if the attribute does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Stores an attribute in this request.
     *
     * @param name specifying the name of the attribute
     * @param value the {@code Object} to be stored
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a {@code Collection} containing the
     * names of the attributes available to this request.
     * This method returns an empty {@code Collection}
     * if the request has no attributes available to it.
     *
     * @return the attribute names
     */
    Collection<String> getAttributeNames();

    /**
     * Removes an attribute from this request.
     *
     * @param name a {@code String} specifying the name of the attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Returns an immutable map of the attributes,
     * with attribute names as map keys and attribute value as map value.
     *
     * @return an unmodifiable map of the attributes
     */
    Map<String, Object> getAllAttributes();

    /**
     * Copies all of the mappings from the specified attributes.
     *
     * @param attributes the specified attributes
     */
    void putAllAttributes(Map<String, Object> attributes);

    /**
     * Extracts all the attributes and fills in the specified map.
     *
     * @param targetAttributes the target attribute map to be filled
     * @since 2.0.0
     */
    void extractAttributes(Map<String, Object> targetAttributes);

    /**
     * Returns the name of the character encoding used in the body of this request.
     *
     * @return a {@code String} containing the name of the character encoding,
     *         or {@code null} if the request does not specify a character encoding
     */
    String getEncoding();

    /**
     * Overrides the name of the character encoding used in the body of this request.
     * This method must be called prior to reading request parameters
     * or reading input using getReader(). Otherwise, it has no effect.
     *
     * @param encoding a {@code String} containing the name of the character encoding.
     * @throws UnsupportedEncodingException if the specified encoding is invalid
     */
    void setEncoding(String encoding) throws UnsupportedEncodingException;

    /**
     * Returns the preferred {@code Locale}.
     *
     * @return a preferred {@code Locale}
     */
    Locale getLocale();

    /**
     * Sets the preferred {@code Locale}.
     *
     * @param locale a given {@code Locale}
     */
    void setLocale(Locale locale);

    /**
     * Returns the preferred {@code TimeZone}.
     *
     * @return a preferred {@code TimeZone}
     */
    TimeZone getTimeZone();

    /**
     * Sets the preferred {@code TimeZone}.
     *
     * @param timeZone a given {@code TimeZone}
     */
    void setTimeZone(TimeZone timeZone);

    /**
     * Returns whether request header has exceed the maximum length.
     *
     * @return true, if max length exceeded
     */
    boolean isMaxLengthExceeded();

    /**
     * Sets whether request header has exceed the maximum length.
     *
     * @param maxLengthExceeded whether the max length exceeded
     */
    void setMaxLengthExceeded(boolean maxLengthExceeded);

}
