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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.rule.ForwardResponseRule;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.type.ResponseType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class ForwardResponse implements Responsible {

	private final Logger logger = LoggerFactory.getLogger(ForwardResponse.class);
	
	private final boolean debugEnabled = logger.isDebugEnabled();
	
	/** The forward response rule. */
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
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.AspectranActivity)
	 */
	public void response(CoreActivity activity) {
		RequestAdapter requestAdapter = activity.getRequestAdapter();

		Map<String, Object> valueMap = null;
		
		if(forwardResponseRule.getParameterItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(activity);
			valueMap = expressor.express(forwardResponseRule.getParameterItemRuleMap());
		}
		
		if(valueMap != null) {
			for(Map.Entry<String, Object> entry : valueMap.entrySet())
				requestAdapter.setAttribute(entry.getKey(), entry.getValue());
		}
		
		if(debugEnabled) {
			logger.debug("response " + forwardResponseRule);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getResponseType()
	 */
	public ResponseType getResponseType() {
		return ForwardResponseRule.RESPONSE_TYPE;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getContentType()
	 */
	public String getContentType() {
		if(forwardResponseRule == null)
			return null;
		
		return forwardResponseRule.getContentType();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
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
