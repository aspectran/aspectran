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
package com.aspectran.core.rule;

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.rule.ability.ActionPossessable;
import com.aspectran.core.token.Token;
import com.aspectran.core.token.Tokenizer;
import com.aspectran.core.type.ResponseType;
import com.aspectran.core.type.TokenType;

import java.util.List;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class RedirectResponseRule extends ActionPossessSupport implements ActionPossessable {
	
	/** The Constant RESPONSE_TYPE. */
	public static final ResponseType RESPONSE_TYPE = ResponseType.REDIRECT;

	/** The id. */
	private String id;
	
	/** The content type. */
	private String contentType;
	
	/** The translet name. */
	private String transletName;
	
	/** The url. */
	private String url;
	
	/** The url tokens. */
	private Token[] urlTokens;
	
	/** The exclude null parameters. */
	private Boolean excludeNullParameters;

	/** The character encoding. */
	private String characterEncoding;
	
	/** The parameter item rule map. */
	private ItemRuleMap parameterItemRuleMap;

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

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
	 * Gets the translet name.
	 *
	 * @return the translet name
	 */
	public String getTransletName() {
		return transletName;
	}

	/**
	 * Sets the translet name.
	 *
	 * @param transletName the new translet name
	 */
	public void setTransletName(String transletName) {
		this.transletName = transletName;
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
		this.urlTokens = null;
		
		if(transletName != null)
			transletName = null;
		
		List<Token> tokens = Tokenizer.tokenize(url, true);
		
		int tokenCount = 0;
		
		for(Token t : tokens) {
			if(t.getType() != TokenType.TEXT)
				tokenCount++;
		}
		
		if(tokenCount > 0)
			this.urlTokens = tokens.toArray(new Token[tokens.size()]);
	}

	/**
	 * Gets the url tokens.
	 * 
	 * @return the url tokens
	 */
	public Token[] getUrlTokens() {
		return urlTokens;
	}

	/**
	 * Gets the exclude null parameters.
	 * 
	 * @return the exclude null parameters
	 */
	public Boolean getExcludeNullParameters() {
		return excludeNullParameters;
	}

	/**
	 * Sets the exclude null parameters.
	 * 
	 * @param excludeNullParameters the new exclude null parameters
	 */
	public void setExcludeNullParameters(Boolean excludeNullParameters) {
		this.excludeNullParameters = excludeNullParameters;
	}

	/**
	 * Gets the character encoding.
	 * 
	 * @return the character encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets the character encoding.
	 * 
	 * @param characterEncoding the new character encoding
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Gets the parameter rule map.
	 * 
	 * @return the parameter rule map
	 */
	public ItemRuleMap getParameterItemRuleMap() {
		return parameterItemRuleMap;
	}

	/**
	 * Sets the parameter rules.
	 * 
	 * @param parameterItemRuleMap the new parameter rules
	 */
	public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
		this.parameterItemRuleMap = parameterItemRuleMap;
	}
	
	/**
	 * Adds the parameter rule.
	 * 
	 * @param parameterItemRule the parameter rule
	 */
	public void addParameterItemRule(ItemRule parameterItemRule) {
		if(parameterItemRuleMap == null) 
			parameterItemRuleMap = new ItemRuleMap();
		
		parameterItemRuleMap.putItemRule(parameterItemRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{id=").append(id);
		sb.append(", contentType=").append(contentType);
		sb.append(", translet=").append(transletName);
		sb.append(", url=").append(url);
		sb.append(", excludeNullParameters=").append(excludeNullParameters);

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
}
