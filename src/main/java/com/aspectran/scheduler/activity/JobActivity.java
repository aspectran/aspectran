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
package com.aspectran.scheduler.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.RequestRule;

import java.util.Map;

/**
 * The Class JobActivity.
 * 
 * <p>Created: 2013. 11. 18 오후 3:40:48</p>
 */
public class JobActivity extends CoreActivity implements Activity {

	public JobActivity(ActivityContext context, RequestAdapter requestAdapter, ResponseAdapter responseAdapter) {
		super(context);

		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
	}

	protected void request(Translet translet) {
		try {
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
	
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		JobActivity activity = new JobActivity(getActivityContext(), getRequestAdapter(), getResponseAdapter());
		return (T)activity;
	}

}
