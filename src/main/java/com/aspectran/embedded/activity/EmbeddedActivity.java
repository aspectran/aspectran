/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.embedded.activity;

import java.io.Writer;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.embedded.adapter.EmbeddedRequestAdapter;
import com.aspectran.embedded.adapter.EmbeddedResponseAdapter;
import com.aspectran.embedded.service.EmbeddedAspectranService;

/**
 * The Class EmbeddedActivity.
 */
public class EmbeddedActivity extends CoreActivity {
	
	private final EmbeddedAspectranService service;

	private final Writer outputWriter;

	private ParameterMap parameterMap;

	private Map<String, Object> attributeMap;

	/**
	 * Instantiates a new embedded activity.
	 *
	 * @param service the embedded aspectran service
	 * @param outputWriter the output writer
	 */
	public EmbeddedActivity(EmbeddedAspectranService service, Writer outputWriter) {
		super(service.getActivityContext());
		setSessionAdapter(service.getSessionAdapter());

		this.service = service;
		this.outputWriter = outputWriter;
	}

	public void setParameterMap(ParameterMap parameterMap) {
		this.parameterMap = parameterMap;
	}

	public void setAttributeMap(Map<String, Object> attributeMap) {
		this.attributeMap = attributeMap;
	}

	@Override
	protected void adapt() throws AdapterException {
		try {
			RequestAdapter requestAdapter = new EmbeddedRequestAdapter(parameterMap);
			setRequestAdapter(requestAdapter);

			ResponseAdapter responseAdapter = new EmbeddedResponseAdapter(outputWriter);
			setResponseAdapter(responseAdapter);

			if(attributeMap != null) {
				for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
					requestAdapter.setAttribute(entry.getKey(), entry.getValue());
				}
			}
		} catch (Exception e) {
			throw new AdapterException("Could not adapt to embedded application activity.", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		EmbeddedActivity activity = new EmbeddedActivity(service, outputWriter);
		activity.setIncluded(true);
		return (T)activity;
	}

}
