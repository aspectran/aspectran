/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.activity.response;

import java.util.Map;

import com.aspectran.base.adapter.RequestAdapter;
import com.aspectran.base.rule.ForwardResponseRule;
import com.aspectran.base.token.expression.ValueExpression;
import com.aspectran.base.token.expression.ValueExpressor;
import com.aspectran.base.type.ResponseType;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.translet.Translet;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class ForwardResponse implements Responsible {

	private final ForwardResponseRule forwardResponseRule;
	
	/**
	 * Instantiates a new forward response.
	 * 
	 * @param forwardResponseRule the forward response rule
	 */
	public ForwardResponse(ForwardResponseRule forwardResponseRule) {
		this.forwardResponseRule = forwardResponseRule;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(Activity activity) {
		Translet translet = activity.getActivityTranslet();
		RequestAdapter requestAdapter = translet.getRequestAdapter();

		Map<String, Object> valueMap = null;
		
		if(forwardResponseRule.getParameterItemRuleMap() != null) {
			ValueExpressor expressor = new ValueExpression(activity);
			valueMap = expressor.express(forwardResponseRule.getParameterItemRuleMap());
		}
		
		if(valueMap != null) {
			for(Map.Entry<String, Object> entry : valueMap.entrySet())
				requestAdapter.setAttribute(entry.getKey(), entry.getValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getResponseType()
	 */
	public ResponseType getResponseType() {
		return ForwardResponseRule.RESPONSE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getId()
	 */
	public String getId() {
		if(forwardResponseRule == null)
			return null;
		
		return forwardResponseRule.getId();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getContentType()
	 */
	public String getContentType() {
		if(forwardResponseRule == null)
			return null;
		
		return forwardResponseRule.getContentType();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return forwardResponseRule.getActionList();
	}

	/**
	 * Gets the forward response rule.
	 * 
	 * @return the forward response rule
	 */
	public ForwardResponseRule getForwardResponseRule() {
		return forwardResponseRule;
	}
	
}
