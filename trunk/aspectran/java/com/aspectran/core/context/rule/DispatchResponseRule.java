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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.BooleanUtils;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class DispatchResponseRule extends ActionPossessSupport implements ActionPossessable {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.DISPATCH;
	
	private String contentType;

	protected String characterEncoding;

	private TemplateRule templateRule;
	
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

	public TemplateRule getTemplateRule() {
		return templateRule;
	}

	public void setTemplateRule(TemplateRule templateRule) {
		this.templateRule = templateRule;
		
		if(templateRule.getEncoding() != null && characterEncoding == null)
			characterEncoding = templateRule.getEncoding();
	}

	public Boolean getDefaultResponse() {
		return defaultResponse;
	}

	public boolean isDefaultResponse() {
		return BooleanUtils.toBoolean(defaultResponse);
	}

	public void setDefaultResponse(Boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{contentType=").append(contentType);
		sb.append(", characterEncoding=").append(characterEncoding);
		sb.append(", templateRule=").append(templateRule);
		if(defaultResponse != null)
			sb.append(", defaultResponse=").append(defaultResponse);
		
		if(actionList != null) {
			sb.append(", actionList=");
			sb.append('[');

			for(int i = 0; i < actionList.size(); i++) {
				Executable action = actionList.get(i);

				if(i > 0)
					sb.append(", ");

				sb.append(action.getActionId());
			}

			sb.append(']');
		}

		sb.append("}");
		
		return sb.toString();
	}
	
	public static DispatchResponseRule newInstance(String contentType, String characterEncoding, Boolean defaultResponse) {
		DispatchResponseRule drr = new DispatchResponseRule();
		drr.setContentType(contentType);
		drr.setCharacterEncoding(characterEncoding);
		drr.setDefaultResponse(defaultResponse);

		return drr;
	}
	
}
