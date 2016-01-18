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
package com.aspectran.console.activity;

import com.aspectran.console.adapter.ConsoleRequestAdapter;
import com.aspectran.console.adapter.ConsoleResponseAdapter;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemTokenExpression;
import com.aspectran.core.context.expr.ItemTokenExpressor;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.variable.ValueMap;

/**
 * The Class ConsoleActivity.
 *
 * @since 2008. 04. 28
 */
public class ConsoleActivity extends CoreActivity implements Activity {

	/**
	 * Instantiates a new ConsoleActivity.
	 *
	 * @param context the current ActivityContext
	 */
	public ConsoleActivity(ActivityContext context) {
		super(context);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#adapting(com.aspectran.core.activity.Translet)
	 */
	protected void adapting(Translet translet) {
		RequestAdapter requestAdapter = new ConsoleRequestAdapter(this);
		setRequestAdapter(requestAdapter);

		ResponseAdapter responseAdapter = new ConsoleResponseAdapter(this);
		setResponseAdapter(responseAdapter);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getSessionAdapter()
	 */
	public synchronized SessionAdapter getSessionAdapter() {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#request(com.aspectran.core.activity.Translet)
	 */
	protected void request(Translet translet) {
        ValueMap valueMap = parseDeclaredParameter();
        if(valueMap != null)
        	translet.setDeclaredAttributeMap(valueMap);
	}
	
	/**
	 * Parses the parameter.
	 *
	 * @return the value map
	 */
	private ValueMap parseDeclaredParameter() {
		ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();

		if(attributeItemRuleMap != null) {
			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				System.out.printf("@ %s: $s", itemRule.getName(), itemRule.toString());
			}
			
			
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueMap valueMap = expressor.express(attributeItemRuleMap);

			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				String name = itemRule.getName();
				Object value = valueMap.get(name);
				if(value != null) {
					getRequestAdapter().setAttribute(name, value);
				}
			}

			if(valueMap.size() > 0)
				return valueMap;
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#newActivity()
	 */
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		ConsoleActivity consoleActivity = new ConsoleActivity(getActivityContext());
		return (T)consoleActivity;
	}

}
