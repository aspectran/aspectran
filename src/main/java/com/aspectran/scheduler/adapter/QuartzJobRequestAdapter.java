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
package com.aspectran.scheduler.adapter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDetail;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

/**
 * The Class QuartzJobRequestAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	private String characterEncoding;

	private Map<String, Object> attributeMap = new HashMap<String, Object>();
	
	/**
	 * Instantiates a new QuartzJobRequestAdapter.
	 *
	 * @param jobDetail the job detail
	 */
	public QuartzJobRequestAdapter(JobDetail jobDetail) {
		super(jobDetail);
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public String getParameter(String name) {
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		// ignore
	}

	@Override
	public String[] getParameterValues(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributeMap.get(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
	
}
