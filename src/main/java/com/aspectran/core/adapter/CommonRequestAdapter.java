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

import com.aspectran.core.activity.request.CommonRequest;

/**
 * The Class CommonRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public abstract class CommonRequestAdapter extends CommonRequest implements RequestAdapter {

	protected final Object adaptee;

	/**
	 * Instantiates a new CommonRequestAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public CommonRequestAdapter(Object adaptee) {
		super();
		this.adaptee = adaptee;
	}

	/**
	 * Instantiates a new CommonRequestAdapter.
	 *
	 * @param adaptee the adaptee
	 * @param parameterMap the params
	 */
	public CommonRequestAdapter(Object adaptee, Map<String, String[]> parameterMap) {
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

	@Override
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
