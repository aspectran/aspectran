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
package com.aspectran.core.activity;

import java.util.HashMap;

import com.aspectran.core.adapter.RequestAdapter;

/**
 * The Class ActivityResultDataMap.
 */
public class ActivityResultDataMap extends HashMap<String, Object> {

	/** @serial */
	private static final long serialVersionUID = -4557424414862800204L;

	protected final Activity activity;

	protected RequestAdapter requestAdapter;

	public ActivityResultDataMap(Activity activity) {
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

}
