/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.scheduler.adapter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import org.quartz.JobDetail;

import com.aspectran.core.activity.variable.AttributeMap;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

/**
 * The Class QuartzJobRequestAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	private String characterEncoding;
	
	private AttributeMap attributeMap = new AttributeMap();
	
	/**
	 * Instantiates a new quartz job request adapter.
	 *
	 * @param jobDetail the job detail
	 */
	public QuartzJobRequestAdapter(JobDetail jobDetail) {
		super(jobDetail);
	}
	
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}
	
	public String getParameter(String name) {
		throw new UnsupportedOperationException("getParameter");
	}
	
	public String[] getParameterValues(String name) {
		throw new UnsupportedOperationException("getParameterValues");
	}
	
	public Enumeration<String> getParameterNames() {
		throw new UnsupportedOperationException("getParameterNames");
	}
	
	public <T> T getAttribute(String name) {
		return attributeMap.getValue(name);
	}
	
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}
	
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());

	}

	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
	
	public Map<String, Object> getParameterMap() {
		throw new UnsupportedOperationException("getParameterMap");
	}
	
}
