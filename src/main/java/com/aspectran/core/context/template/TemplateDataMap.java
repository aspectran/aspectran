/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.template;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.RequestAdapter;

public class TemplateDataMap extends HashMap<String, Object> {

	/** @serial */
	private static final long serialVersionUID = 4521506828465323127L;
	
	private final Activity activity;

	private RequestAdapter requestAdapter;

	private Map<String, Object> attributeMap;

	public TemplateDataMap(Activity activity) {
		this.activity = activity;
		this.requestAdapter = activity.getRequestAdapter();
	}

	public Object get(Object key) {
		String name = key.toString();
		Object value = super.get(key);

		if(value != null)
			return value;

		if(activity.getProcessResult() != null) {
			value = activity.getProcessResult().getResultValue(name);

			if(value != null) {
				put(name, value);
				return value;
			}
		}

		value = requestAdapter.getAttribute(name);

		if(value != null) {
			put(name, value);
			return value;
		}

		value = requestAdapter.getParameter(name);

		if(value != null) {
			put(name, value);
			return value;
		}

		return null;
	}

	public Locale getLocale() {
		if(requestAdapter != null)
			return requestAdapter.getLocale();

		return null;
	}

}
