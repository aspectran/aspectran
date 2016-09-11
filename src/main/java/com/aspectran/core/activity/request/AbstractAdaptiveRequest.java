/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.request.parameter.FileParameterMap;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.core.util.MultiValueMap;

/**
 * The Class AbstractRequest.
 *
 * @since 2011. 3. 12.
 */
public abstract class AbstractAdaptiveRequest {

	private MethodType requestMethod;

	private MultiValueMap<String, String> headers;

	private ParameterMap parameterMap;

	private FileParameterMap fileParameterMap;

	private Locale locale;

	private TimeZone timeZone;

	private boolean maxLengthExceeded;

	public AbstractAdaptiveRequest() {
	}

	public AbstractAdaptiveRequest(Map<String, String[]> parameterMap) {
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
	 * Returns a map of the request headers that can be modified.
	 * If not yet instantiated then create a new one.
	 *
	 * @return an {@code MultiValueMap} object, may not be {@code null}
	 */
	protected MultiValueMap<String, String> touchHeaders() {
		if (headers == null) {
			headers = new LinkedCaseInsensitiveMultiValueMap<String>(12);
		}
		return headers;
	}

	protected boolean isHeadersInstantiated() {
		return (headers != null);
	}

	/**
	 * Returns a map of the request headers that can be modified.
	 *
	 * @return an {@code MultiValueMap} object, may be {@code null}
	 */
	public MultiValueMap<String, String> getHeaders() {
		return touchHeaders();
	}

	/**
	 * Returns the value of the response header with the given name.
	 *
	 * <p>If a response header with the given name exists and contains
	 * multiple values, the value that was added first will be returned.
	 *
	 * @param name the name of the response header whose value to return
	 * @return the value of the response header with the given name,
	 * 		or {@code null} if no header with the given name has been set
	 * 		on this response
	 */
	public String getHeader(String name) {
		return touchHeaders().getFirst(name);
	}

	/**
	 * Returns the values of the response header with the given name.
	 *
	 * @param name the name of the response header whose values to return
	 * @return a (possibly empty) {@code Collection} of the values
	 * 		of the response header with the given name
	 */
	public Collection<String> getHeaders(String name) {
		return touchHeaders().get(name);
	}

	/**
	 * Returns the names of the headers of this response.
	 *
	 * @return a (possibly empty) {@code Collection} of the names
	 * 		of the headers of this response
	 */
	public Collection<String> getHeaderNames() {
		return touchHeaders().keySet();
	}

	/**
	 * Returns a boolean indicating whether the named response header
	 * has already been set.
	 *
	 * @param name the header name
	 * @return {@code true} if the named response header
	 * 		has already been set; {@code false} otherwise
	 */
	public boolean containsHeader(String name) {
		List<String> values = touchHeaders().get(name);
		return (values != null && !values.isEmpty());
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

	public String getParameter(String name) {
		return (parameterMap == null ? null : parameterMap.getParameter(name));
	}

	public void setParameter(String name, String value) {
		touchParameterMap().setParameter(name, value);
	}

	public String[] getParameterValues(String name) {
		return (parameterMap == null ? null : parameterMap.getParameterValues(name));
	}

	public void setParameter(String name, String[] values) {
		touchParameterMap().put(name, values);
	}

	private ParameterMap touchParameterMap() {
		if (this.parameterMap == null) {
			this.parameterMap = new ParameterMap();
		}
		return this.parameterMap;
	}

	public Map<String, Object> getParameterMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		fillPrameterMap(params);
		return params;
	}

	public Enumeration<String> getParameterNames() {
		return (parameterMap == null ? null : parameterMap.getParameterNames());
	}

	public void fillPrameterMap(Map<String, Object> targetParameterMap) {
		if (this.parameterMap != null) {
			for (Map.Entry<String, String[]> entry : this.parameterMap.entrySet()) {
				String name = entry.getKey();
				String[] values = entry.getValue();
				if (values.length == 1) {
					targetParameterMap.put(name, values[0]);
				} else {
					targetParameterMap.put(name, values);
				}
			}
		}
	}

	public FileParameter getFileParameter(String name) {
		return (fileParameterMap != null ? fileParameterMap.getFileParameter(name) : null);
	}
	
	public FileParameter[] getFileParameterValues(String name) {
		return (fileParameterMap != null ? fileParameterMap.getFileParameterValues(name) : null);
	}

	public void setFileParameter(String name, FileParameter fileParameter) {
		touchFileParameterMap().setFileParameter(name, fileParameter);
	}
	
	public void setFileParameter(String name, FileParameter[] fileParameters) {
		touchFileParameterMap().setFileParameter(name, fileParameters);
	}
	
	public Enumeration<String> getFileParameterNames() {
		FileParameterMap fileParameterMap = touchFileParameterMap();
		return (fileParameterMap != null ? Collections.enumeration(fileParameterMap.keySet()) : null);
	}
	
	public FileParameter[] removeFileParameter(String name) {
		return (fileParameterMap != null ? fileParameterMap.remove(name) : null);
	}
	
	private FileParameterMap touchFileParameterMap() {
		if (fileParameterMap == null) {
			fileParameterMap = new FileParameterMap();
		}
		return fileParameterMap;
	}

	public Map<String, Object> getAttributeMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		fillAttributeMap(params);
		return params;
	}

	public void fillAttributeMap(Map<String, Object> targetAttributeMap) {
		if (targetAttributeMap != null) {
			Enumeration<String> enm = getAttributeNames();

			while (enm.hasMoreElements()) {
				String name = enm.nextElement();
				Object value = getAttribute(name);
				targetAttributeMap.put(name, value);
			}
		}
	}

	public abstract <T> T getAttribute(String name);

	public abstract void setAttribute(String name, Object value);

	public abstract Enumeration<String> getAttributeNames();

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
