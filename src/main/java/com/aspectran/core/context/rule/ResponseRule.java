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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformFactory;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.ResponseType;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * The Class ResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class ResponseRule implements ResponseRuleApplicable, Replicable<ResponseRule> {

	public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";

	private String name;
	
	private String characterEncoding;
	
	private Response response;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	/**
	 * Instantiates a new ResponseRule.
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

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public ResponseType getResponseType() {
		if(response == null)
			return null;
		
		return response.getResponseType();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRespondent() {
		return (T)response;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param tr the new default response rule
	 * 
	 * @return the transform response
	 */
	public Response applyResponseRule(TransformRule tr) {
		Response response = TransformFactory.createTransform(tr);
		
		this.response = response;
		
		return response;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param drr the new default response rule
	 * 
	 * @return the dispatch response
	 */
	public Response applyResponseRule(DispatchResponseRule drr) {
		Response response = new DispatchResponse(drr);
		
		this.response = response;
		
		return response;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param rrr the new default response rule
	 * 
	 * @return the redirect response
	 */
	public Response applyResponseRule(RedirectResponseRule rrr) {
		Response response = new RedirectResponse(rrr);

		this.response = response;
		
		return response;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param frr the new default response rule
	 * 
	 * @return the forward response
	 */
	public Response applyResponseRule(ForwardResponseRule frr) {
		Response response = new ForwardResponse(frr);

		this.response = response;
		
		return response;
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

	public ResponseRule newUrgentResponseRule(Response response) {
		ResponseRule responseRule = new ResponseRule();
		responseRule.setCharacterEncoding(characterEncoding);
		responseRule.setResponse(response);
		return responseRule;
	}
	
	public ResponseRule replicate() {
		return replicate(this);
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

	public static ResponseRule replicate(ResponseRule responseRule) {
		ResponseRule newResponseRule = new ResponseRule();
		newResponseRule.setName(responseRule.getName());
		newResponseRule.setCharacterEncoding(responseRule.getCharacterEncoding());
		
		Response response = responseRule.getResponse();
		if(response != null) {
			Response newResponse = response.replicate();
			newResponseRule.setResponse(newResponse);
		}
		
		return newResponseRule;
	}
	
}
