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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformFactory;
import com.aspectran.core.var.rule.ability.ResponseRuleApplicable;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class ResponseByContentTypeRule implements ResponseRuleApplicable {

	private ResponseMap responseMap = new ResponseMap();
	
	private Responsible defaultResponse;

	private String exceptionType;
	
	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	public Responsible getResponse(String contentType) {
		Responsible response = responseMap.get(contentType);
		
		if(response != null)
			return response;
		
		return defaultResponse;
	}
	
	/**
	 * Gets the response map.
	 * 
	 * @return the response map
	 */
	public ResponseMap getResponseMap() {
		return responseMap;
	}
	
	/**
	 * Sets the response map.
	 * 
	 * @param responseMap the new response map
	 */
	public void setResponseMap(ResponseMap responseMap) {
		this.responseMap = responseMap;
	}
	
	public Responsible getDefaultResponse() {
		return defaultResponse;
	}

	public void setDefaultResponse(Responsible defaultResponse) {
		this.defaultResponse = defaultResponse;
	}

	/**
	 * Adds the response rule.
	 * 
	 * @param transformRule the tr
	 * 
	 * @return the transform response
	 */
	public Responsible applyResponseRule(TransformRule transformRule) {
		Responsible response = TransformFactory.createTransform(transformRule);
		
		if(transformRule.getContentType() != null)
			responseMap.put(transformRule.getContentType(), response);
		else
			defaultResponse = response;
		
		return response;
	}

	/**
	 * Adds the response rule.
	 * 
	 * @param dispatchResponseRule the drr
	 * 
	 * @return the dispatch response
	 */
	public Responsible applyResponseRule(DispatchResponseRule dispatchResponseRule) {
		Responsible response = new DispatchResponse(dispatchResponseRule);
		
		if(dispatchResponseRule.getContentType() != null)
			responseMap.put(dispatchResponseRule.getContentType(), response);
		else
			defaultResponse = response;
		
		return response;
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param redirectResponseRule the rrr
	 * 
	 * @return the redirect response
	 */
	public Responsible applyResponseRule(RedirectResponseRule redirectResponseRule) {
		Responsible response = new RedirectResponse(redirectResponseRule);

		if(redirectResponseRule.getContentType() != null)
			responseMap.put(redirectResponseRule.getContentType(), response);
		else
			defaultResponse = response;
		
		return response;
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param forwardResponseRule the frr
	 * 
	 * @return the forward response
	 */
	public Responsible applyResponseRule(ForwardResponseRule forwardResponseRule) {
		Responsible response = new ForwardResponse(forwardResponseRule);

		if(forwardResponseRule.getContentType() != null)
			responseMap.put(forwardResponseRule.getContentType(), response);
		else
			defaultResponse = response;
		
		return response;
	}

	/**
	 * Adds the response.
	 * 
	 * @param resonse the resonse
	 */
	public void addResponse(Responsible response) {
		responseMap.put(response.getContentType(), response);
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param transformRule the new default response rule
	 * 
	 * @return the transform response
	 */
	public Responsible setDefaultResponse(TransformRule transformRule) {
		Responsible response = TransformFactory.createTransform(transformRule);
		
		this.defaultResponse = response;
		
		return response;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param dispatchResponseRule the new default response rule
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse setDefaultResponse(DispatchResponseRule dispatchResponseRule) {
		DispatchResponse dispatchResponse = new DispatchResponse(dispatchResponseRule);
		
		this.defaultResponse = dispatchResponse;
		
		return dispatchResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param redirectResponseRule the new default response rule
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse setDefaultResponse(RedirectResponseRule redirectResponseRule) {
		RedirectResponse redirectResponse = new RedirectResponse(redirectResponseRule);

		this.defaultResponse = redirectResponse;
		
		return redirectResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param forwardResponseRule the new default response rule
	 * 
	 * @return the forward response
	 */
	public ForwardResponse setDefaultResponse(ForwardResponseRule forwardResponseRule) {
		ForwardResponse forwardResponse = new ForwardResponse(forwardResponseRule);

		this.defaultResponse = forwardResponse;
		
		return forwardResponse;
	}
	
	public static ResponseByContentTypeRule newInstance(String exceptionType) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
		if(exceptionType != null)
			rbctr.setExceptionType(exceptionType);
		
		return rbctr;
	}
	
}
