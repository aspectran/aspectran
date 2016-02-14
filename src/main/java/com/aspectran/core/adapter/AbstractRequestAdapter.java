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
package com.aspectran.core.adapter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.activity.request.AbstractRequest;

/**
 * The Class AbstractRequestAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

	protected final Object adaptee;

	/**
	 * Instantiates a new AbstractRequestAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractRequestAdapter(Object adaptee) {
		super();
		this.adaptee = adaptee;
	}

	/**
	 * Instantiates a new AbstractRequestAdapter.
	 *
	 * @param adaptee the adaptee
	 * @param parameterMap the params
	 */
	public AbstractRequestAdapter(Object adaptee, Map<String, String[]> parameterMap) {
		super(parameterMap);
		this.adaptee = adaptee;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}

	@Override
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		fillAttributeMap(params);
		return params;
	}
	
	public void fillAttributeMap(Map<String, Object> attributeMap) {
		if(attributeMap == null)
			return;
		
		Enumeration<String> enm = getAttributeNames();
		
		while(enm.hasMoreElements()) {
			String name = enm.nextElement();
			Object value = getAttribute(name);
			attributeMap.put(name, value);
		}
	}
	
}
