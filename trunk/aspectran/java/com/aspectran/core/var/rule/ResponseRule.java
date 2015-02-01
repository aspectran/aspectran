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
package com.aspectran.core.var.rule;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.var.rule.ability.ResponseSettable;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:48:09
 * </p>
 */
public class ResponseRule implements ResponseSettable, AspectAdviceSupport {

	public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";

	public static final String VIEW_DISPATCHER_SETTING_NAME = "viewDispatcher";
	
	private String name;
	
	private String characterEncoding;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	private Responsible response;
	
	/**
	 * Instantiates a new response rule.
	 */
	public ResponseRule() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Responsible getResponse() {
		return response;
	}

	public void setResponse(Responsible response) {
		this.response = response;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param tr the new default response rule
	 * 
	 * @return the transform response
	 */
	public AbstractTransform setResponse(TransformRule tr) {
		AbstractTransform transformResponse = AbstractTransform.createTransformer(tr);
		
		this.response = transformResponse;
		
		return transformResponse;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param drr the new default response rule
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse setResponse(DispatchResponseRule drr) {
		DispatchResponse dispatchResponse = new DispatchResponse(drr);
		
		this.response = dispatchResponse;
		
		return dispatchResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param rrr the new default response rule
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse setResponse(RedirectResponseRule rrr) {
		RedirectResponse redirectResponse = new RedirectResponse(rrr);

		this.response = redirectResponse;
		
		return redirectResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param frr the new default response rule
	 * 
	 * @return the forward response
	 */
	public ForwardResponse setResponse(ForwardResponseRule frr) {
		ForwardResponse forwardResponse = new ForwardResponse(frr);

		this.response = forwardResponse;
		
		return forwardResponse;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return (AspectAdviceRuleRegistry)aspectAdviceRuleRegistry.clone();
		
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
	}

	public ResponseRule newResponseRule(Responsible response) {
		ResponseRule responseRule = new ResponseRule();
		responseRule.setCharacterEncoding(characterEncoding);
		responseRule.setResponse(response);
		return responseRule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{name=").append(name);
		sb.append(", characterEncoding=").append(characterEncoding);
		sb.append(", response=").append(response);
		sb.append("} ");
		
		return sb.toString();
	}
	
	public static ResponseRule newInstance(String name, String characterEncoding) {
		if(characterEncoding != null && !Charset.isSupported(characterEncoding))
			throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
		
		ResponseRule responseRule = new ResponseRule();
		responseRule.setName(name);
		responseRule.setCharacterEncoding(characterEncoding);
		
		return responseRule;
	}

}
