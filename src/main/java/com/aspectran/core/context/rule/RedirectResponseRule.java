/**
 * Copyright 2008-2017 Juho Jeong
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

import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import com.aspectran.core.context.rule.ability.ActionPossessSupport;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class RedirectResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class RedirectResponseRule extends ActionPossessSupport implements Replicable<RedirectResponseRule> {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.REDIRECT;

	private String contentType;
	
	private String target;
	
	private Token[] targetTokens;
	
	private Boolean excludeNullParameter;

	private String characterEncoding;
	
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
	 * Gets the redirect target.
	 * 
	 * @return the redirect target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Gets the redirect target.
	 *
	 * @param activity the activity
	 * @return the redirect target
	 */
	public String getTarget(Activity activity) {
		if (targetTokens != null && targetTokens.length > 0) {
			TokenEvaluator evaluator = new TokenExpressionParser(activity);
			return evaluator.evaluateAsString(targetTokens);
		} else {
			return target;
		}
	}

	/**
	 * Sets the target name.
	 * 
	 * @param target the new target name
	 */
	public void setTarget(String target) {
		this.target = target;

		List<Token> tokens = Tokenizer.tokenize(target, true);
		int tokenCount = 0;
		
		for (Token t : tokens) {
			if (t.getType() != TokenType.TEXT) {
				tokenCount++;
			}
		}
		
		if (tokenCount > 0) {
			this.targetTokens = tokens.toArray(new Token[tokens.size()]);
		} else {
			this.targetTokens = null;
		}
	}
	
	public void setTarget(String target, Token[] targetTokens) {
		this.target = target;
		this.targetTokens = targetTokens;
	}

	/**
	 * Gets the tokens of the redirect target.
	 * 
	 * @return the tokens of the redirect target
	 */
	public Token[] getTargetTokens() {
		return targetTokens;
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
		if (parameterItemRuleMap == null) {
			parameterItemRuleMap = new ItemRuleMap();
		}
		parameterItemRuleMap.putItemRule(parameterItemRule);
	}

	/**
	 * Sets the parameter map.
	 *
	 * @param parameterMap the parameter map
	 */
	public void setParameterMap(Map<String, String> parameterMap) {
		if (parameterMap == null) {
			this.parameterItemRuleMap = null;
			return;
		}

		ItemRuleMap params = new ItemRuleMap();
		for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
			ItemRule ir = new ItemRule();
			ir.setName(entry.getKey());
			ir.setValue(entry.getValue());
			ir.setTokenize(Boolean.FALSE);
		}

		this.parameterItemRuleMap = params;
	}

	/**
	 * Returns whether the default response.
	 *
	 * @return whether the default response
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

	@Override
	public RedirectResponseRule replicate() {
		return replicate(this);
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.appendForce("responseType", RESPONSE_TYPE);
		tsb.appendForce("target", target);
		tsb.append("contentType", contentType);
		tsb.append("excludeNullParameter", excludeNullParameter);
		tsb.append("defaultResponse", defaultResponse);
		return tsb.toString();
	}
	
	public static RedirectResponseRule newInstance(String contentType, String target, Boolean excludeNullParameters, Boolean defaultResponse) {
		RedirectResponseRule rrr = new RedirectResponseRule();
		rrr.setContentType(contentType);
		if (target != null && target.length() > 0) {
			rrr.setTarget(target);
		}
		rrr.setExcludeNullParameter(excludeNullParameters);
		rrr.setDefaultResponse(defaultResponse);
		return rrr;
	}
	
	public static RedirectResponseRule newInstance(String target) {
		if (target == null) {
			throw new IllegalArgumentException("The target argument must not be null.");
		}
		RedirectResponseRule rrr = new RedirectResponseRule();
		rrr.setTarget(target);
		return rrr;
	}
	
	public static RedirectResponseRule replicate(RedirectResponseRule redirectResponseRule) {
		RedirectResponseRule rrr = new RedirectResponseRule();
		rrr.setContentType(redirectResponseRule.getContentType());
		rrr.setTarget(redirectResponseRule.getTarget(), redirectResponseRule.getTargetTokens());
		rrr.setExcludeNullParameter(redirectResponseRule.getExcludeNullParameter());
		rrr.setCharacterEncoding(redirectResponseRule.getCharacterEncoding());
		rrr.setParameterItemRuleMap(redirectResponseRule.getParameterItemRuleMap());
		rrr.setDefaultResponse(redirectResponseRule.getDefaultResponse());
		rrr.setActionList(redirectResponseRule.getActionList());
		return rrr;
	}
	
}
