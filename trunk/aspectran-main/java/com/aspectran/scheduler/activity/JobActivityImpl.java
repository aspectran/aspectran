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

import java.util.Map;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.CoreActivityImpl;
import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.var.ValueMap;
import com.aspectran.core.var.rule.RequestRule;
import com.aspectran.core.var.rule.ResponseRule;
import com.aspectran.core.var.token.ItemTokenExpression;
import com.aspectran.core.var.token.ItemTokenExpressor;

/**
 * <p>Created: 2013. 11. 18 오후 3:40:48</p>
 */
public class JobActivityImpl extends CoreActivityImpl implements JobActivity {

	public JobActivityImpl(ActivityContext context, RequestAdapter requestAdapter, ResponseAdapter responseAdapter) {
		super(context);
		
		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
		
		setTransletInterfaceClass(JobTranslet.class);
		setTransletImplementClass(JobTransletImpl.class);
	}
	
	protected void request(CoreTranslet translet) throws RequestException {
		RequestRule requestRule = getRequestRule();
		ResponseRule responseRule = getResponseRule();
		RequestAdapter requestAdapter = getRequestAdapter();
		ResponseAdapter responseAdapter = getResponseAdapter();
		
		try {
			if(requestAdapter != null) {
				String characterEncoding = requestRule.getCharacterEncoding();
				
				if(characterEncoding == null)
					characterEncoding = (String)getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
				
				if(characterEncoding != null)
					requestAdapter.setCharacterEncoding(characterEncoding);
			}
			
			if(responseAdapter != null) {
				String characterEncoding = responseRule.getCharacterEncoding();

				if(characterEncoding == null)
					characterEncoding = (String)getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);

				if(characterEncoding != null)
					responseAdapter.setCharacterEncoding(characterEncoding);
			}
	
	        ValueMap valueMap = parseParameter();
	        
	        if(valueMap != null)
	        	translet.setDeclaredAttributeMap(valueMap);
        
		} catch(Exception e) {
			throw new RequestException(e);
		}
	}
	
	/**
	 * Parses the parameter.
	 */
	private ValueMap parseParameter() {
		RequestRule requestRule = getRequestRule();
		
		if(requestRule.getAttributeItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueMap valueMap = expressor.express(requestRule.getAttributeItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					getRequestAdapter().setAttribute(entry.getKey(), entry.getValue());
				
				return valueMap;
			}
		}
		
		return null;
	}
	
	public CoreActivity newCoreActivity() {
		JobActivityImpl activity = new JobActivityImpl(getActivityContext(), getRequestAdapter(), getResponseAdapter());
		return activity;
	}

}
