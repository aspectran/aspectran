/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule;

import java.util.List;

import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.Tokenizer;
import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class RedirectResponseRule.
 * 
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class RedirectResponseRule extends ActionPossessSupport implements ActionPossessable {
	
	/** The Constant RESPONSE_TYPE. */
	public static final ResponseType RESPONSE_TYPE = ResponseType.REDIRECT;

	/** The content type. */
	private String contentType;
	
	/** The translet name. */
	private String transletName;
	
	/** The url. */
	private String url;
	
	/** The url tokens. */
	private Token[] urlTokens;
	
	/** The exclude null parameters. */
	private Boolean excludeNullParameter;

	/** The character encoding. */
	private String characterEncoding;
	
	/** The parameter item rule map. */
	private ItemRuleMap parameterItemRuleMap;

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
	public Boolean getExcludeNullParameter() {
		return excludeNullParameter;
	}

	public boolean isExcludeNullParameter() {
		return BooleanUtils.toBoolean(excludeNullParameter);
	}

	/**
	 * Sets the exclude null parameters.
	 * 
	 * @param excludeNullParameter the new exclude null parameters
	 */
	public void setExcludeNullParameter(Boolean excludeNullParameter) {
		this.excludeNullParameter = excludeNullParameter;
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
		sb.append(", translet=").append(transletName);
		sb.append(", url=").append(url);
		sb.append(", excludeNullParameters=").append(excludeNullParameter);
		if(defaultResponse != null)
			sb.append(", defaultResponse=").append(defaultResponse);
		/*
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
		*/
		sb.append("}");
		
		return sb.toString();
	}
	
	public static RedirectResponseRule newInstance(String contentType, String translet, String url, Boolean excludeNullParameters, Boolean defaultResponse) {
		RedirectResponseRule rrr = new RedirectResponseRule();
		rrr.setContentType(contentType);
		rrr.setTransletName(translet);
		
		if(url != null && url.length() > 0)
			rrr.setUrl(url);
		
		rrr.setExcludeNullParameter(excludeNullParameters);
		
		rrr.setDefaultResponse(defaultResponse);

		return rrr;
	}
	
	public static void updateUrl(RedirectResponseRule rrr, String url) {
		if(url != null) {
			url = url.trim();
			
			if(url.length() == 0)
				url = null;
		}

		if(url != null) {
			rrr.setUrl(url);
		}
	}
	
}
