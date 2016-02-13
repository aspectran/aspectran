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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.request.parameter.FileParameterMap;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Class AbstractRequest.
 *
 * @author Juho Jeong
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

	private RequestMethodType requestMethod;

	private ParameterMap parameterMap;

	private FileParameterMap fileParameterMap;

	private Locale locale;

	private boolean maxLengthExceeded;

	public AbstractRequest() {
	}

	public AbstractRequest(Map<String, String[]> parameterMap) {
		if(parameterMap != null && !parameterMap.isEmpty()) {
			this.parameterMap = new ParameterMap(parameterMap);
		}
	}

	public RequestMethodType getRequestMethod() {
		return requestMethod;
	}

	protected void setRequestMethod(RequestMethodType requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getParameter(String name) {
		if(parameterMap == null)
			return null;

		return parameterMap.getParameter(name);
	}

	public void setParameter(String name, String value) {
		String[] values = new String[] { value };
		touchParameterMap().put(name, values);
	}

	public String[] getParameterValues(String name) {
		if(parameterMap == null)
			return null;

		return parameterMap.getParameterValues(name);
	}

	public void setParameter(String name, String[] values) {
		touchParameterMap().put(name, values);
	}

	private ParameterMap touchParameterMap() {
		if(this.parameterMap == null) {
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
		if(parameterMap == null)
			return null;

		return parameterMap.getParameterNames();
	}

	public void fillPrameterMap(Map<String, Object> params) {
		if(this.parameterMap == null)
			return;

		for(Map.Entry<String, String[]> entry : this.parameterMap.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			if(values.length == 1) {
				params.put(name, values[0]);
			} else {
				params.put(name, values);
			}
		}
	}

	public FileParameter getFileParameter(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.getFileParameter(name);
	}
	
	public FileParameter[] getFileParameterValues(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.getFileParameters(name);
	}

	public void setFileParameter(String name, FileParameter fileParameter) {
		touchFileParameterMap().putFileParameter(name, fileParameter);
	}
	
	public void setFileParameter(String name, FileParameter[] fileParameters) {
		touchFileParameterMap().putFileParameter(name, fileParameters);
	}
	
	public Enumeration<String> getFileParameterNames() {
		FileParameterMap fileParameterMap = touchFileParameterMap();

		if(fileParameterMap == null)
			return null;

		return Collections.enumeration(fileParameterMap.keySet());
	}
	
	public FileParameter[] removeFileParameter(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.remove(name);
	}
	
	private FileParameterMap touchFileParameterMap() {
		if(fileParameterMap == null) {
			fileParameterMap = new FileParameterMap();
		}
		
		return fileParameterMap;
	}

	/**
	 * Sets whether request header has exceed the maximum length.
	 *
	 * @param maxLengthExceeded whether request header has exceed the maximum length
	 */
	public void setMaxLengthExceeded(boolean maxLengthExceeded) {
		this.maxLengthExceeded = maxLengthExceeded;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
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
