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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.response.dispatch.ViewDispatcher;
import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class DispatchResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class DispatchResponseRule extends ActionPossessSupport implements ActionPossessable {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.DISPATCH;
	
	private String contentType;

	private String dispatchName;

	private String characterEncoding;
	
	private Boolean defaultResponse;

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type.
	 * 
	 * @param contentType the new content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * Gets the dispatch name.
	 *
	 * @return the dispatch name
	 */
	public String getDispatchName() {
		return dispatchName;
	}

	/**
	 * Sets the dispatch name.
	 *
	 * @param dispatchName the new dispatch name
	 */
	public void setDispatchName(String dispatchName) {
		this.dispatchName = dispatchName;
	}
	
	/**
	 * Gets the character encoding.
	 * 
	 * @return the characterEncoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets the character encoding.
	 * 
	 * @param characterEncoding the characterEncoding to set
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Gets the default response.
	 *
	 * @return the default response
	 */
	public Boolean getDefaultResponse() {
		return defaultResponse;
	}

	/**
	 * Returns whether the default response.
	 *
	 * @return true, if is default response
	 */
	public boolean isDefaultResponse() {
		return BooleanUtils.toBoolean(defaultResponse);
	}

	/**
	 * Sets whether the default response.
	 *
	 * @param defaultResponse whether the default response
	 */
	public void setDefaultResponse(Boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * Returns a string representation of <code>DispatchResponseRule</code> with used <code>Dispatcher</code>.
	 *
	 * @param viewDispatcher the view dispatcher
	 * @return a string representation of <code>DispatchResponseRule</code>.
	 */
	public String toString(ViewDispatcher viewDispatcher) {
		StringBuilder sb = new StringBuilder();
		sb.append("{contentType=").append(contentType);
		sb.append(", dispatchName=").append(dispatchName);
		if(viewDispatcher != null)
			sb.append(", viewDispatcher=").append(viewDispatcher);
		sb.append(", characterEncoding=").append(characterEncoding);
		if(defaultResponse != null)
			sb.append(", defaultResponse=").append(defaultResponse);
		sb.append("}");
		
		return sb.toString();
	}

	/**
	 * Returns a new instance of DispatchResponseRule.
	 *
	 * @param dispatchName the dispatch name
	 * @param contentType the content type
	 * @param characterEncoding the character encoding
	 * @param defaultResponse the default response
	 * @return an instance of DispatchResponseRule
	 */
	public static DispatchResponseRule newInstance(String dispatchName, String contentType, String characterEncoding, Boolean defaultResponse) {
		DispatchResponseRule drr = new DispatchResponseRule();
		drr.setDispatchName(dispatchName);
		drr.setContentType(contentType);
		drr.setCharacterEncoding(characterEncoding);
		drr.setDefaultResponse(defaultResponse);

		return drr;
	}

	/**
	 * Returns a new derived instance of DispatchResponseRule.
	 *
	 * @param dispatchResponseRule an instance of DispatchResponseRulethe
	 * @return the dispatch response rule
	 */
	public static DispatchResponseRule newDerivedDispatchResponseRuleRule(DispatchResponseRule dispatchResponseRule) {
		DispatchResponseRule newDispatchResponseRule = new DispatchResponseRule();
		newDispatchResponseRule.setDispatchName(dispatchResponseRule.getDispatchName());
		newDispatchResponseRule.setContentType(dispatchResponseRule.getContentType());
		newDispatchResponseRule.setCharacterEncoding(dispatchResponseRule.getCharacterEncoding());
		newDispatchResponseRule.setDefaultResponse(dispatchResponseRule.getDefaultResponse());
		newDispatchResponseRule.setActionList(dispatchResponseRule.getActionList());
		
		return newDispatchResponseRule;
	}

}
