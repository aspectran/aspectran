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
package com.aspectran.core.activity.response;

import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ForwardResponse.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class ForwardResponse implements Response {

	private final Log log = LogFactory.getLog(ForwardResponse.class);
	
	private final boolean debugEnabled = log.isDebugEnabled();
	
	private final ForwardResponseRule forwardResponseRule;
	
	/**
	 * Instantiates a new ForwardResponse.
	 * 
	 * @param forwardResponseRule the forward response rule
	 */
	public ForwardResponse(ForwardResponseRule forwardResponseRule) {
		this.forwardResponseRule = forwardResponseRule;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(Activity activity) {
		RequestAdapter requestAdapter = activity.getRequestAdapter();
		
		if(requestAdapter == null)
			return;

		if(debugEnabled) {
			log.debug("response " + forwardResponseRule);
		}

		if(forwardResponseRule.getAttributeItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(activity);
			ValueMap valueMap = expressor.express(forwardResponseRule.getAttributeItemRuleMap());

			for(Map.Entry<String, Object> entry : valueMap.entrySet())
				requestAdapter.setAttribute(entry.getKey(), entry.getValue());
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

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#getTemplateRule()
	 */
	public TemplateRule getTemplateRule() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#newDerivedResponse()
	 */
	public Response newDerivedResponse() {
		return this;
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
