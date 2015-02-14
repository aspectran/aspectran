/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.scheduler.activity;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.activity.CoreActivityImpl;
import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.variable.ValueObjectMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;

/**
 * <p>Created: 2013. 11. 18 오후 3:40:48</p>
 */
public class JobActivityImpl extends CoreActivityImpl implements JobActivity {

	private RequestRule requestRule;
	
	private ResponseRule responseRule;
	
	private RequestAdapter requestAdapter;
	
	private ResponseAdapter responseAdapter;

	public JobActivityImpl(ActivityContext context, RequestAdapter requestAdapter, ResponseAdapter responseAdapter) {
		super(context);
		
		this.requestAdapter = requestAdapter;
		this.responseAdapter = responseAdapter;
		
		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
		
		setTransletInterfaceClass(JobTranslet.class);
		setTransletImplementClass(JobTransletImpl.class);
	}
	
	public void ready(String transletName) throws CoreActivityException {
		super.ready(transletName);

		requestRule = getRequestRule();
		responseRule = getResponseRule();
		
		determineCharacterEncoding();
	}
	
	protected void request(CoreTranslet translet) throws RequestException {
		try {
	        ValueObjectMap valueMap = parseParameter();
	        
	        if(valueMap != null)
	        	translet.setDeclaredAttributeMap(valueMap);
        
		} catch(Exception e) {
			throw new RequestException(e);
		}
	}
	
	private void determineCharacterEncoding() throws CoreActivityException {
		try {
			String characterEncoding = requestRule.getCharacterEncoding();
			
			if(characterEncoding == null)
				characterEncoding = (String)getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
			
			if(characterEncoding != null)
				requestAdapter.setCharacterEncoding(characterEncoding);
		
			characterEncoding = responseRule.getCharacterEncoding();
	
			if(characterEncoding == null)
				characterEncoding = (String)getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);
	
			if(characterEncoding != null)
				responseAdapter.setCharacterEncoding(characterEncoding);
		} catch(UnsupportedEncodingException e) {
			throw new CoreActivityException(e);
		}
	}
	
	/**
	 * Parses the parameter.
	 */
	private ValueObjectMap parseParameter() {
		RequestRule requestRule = getRequestRule();
		
		if(requestRule.getAttributeItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueObjectMap valueMap = expressor.express(requestRule.getAttributeItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					getRequestAdapter().setAttribute(entry.getKey(), entry.getValue());
				
				return valueMap;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CoreActivity> T newActivity() {
		JobActivityImpl activity = new JobActivityImpl(getActivityContext(), getRequestAdapter(), getResponseAdapter());
		return (T)activity;
	}

}
