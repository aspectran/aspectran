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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformFactory;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class ResponseByContentTypeRule implements ResponseRuleApplicable {
	
	private String exceptionType;

	private ResponseMap responseMap = new ResponseMap();
	
	private Response defaultResponse;
	
	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	public Response getResponse(String contentType) {
		Response response = responseMap.get(contentType);
		
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
	
	public Response getDefaultResponse() {
		return defaultResponse;
	}

	public void setDefaultResponse(Response response) {
		this.defaultResponse = response;
	}

	/**
	 * Adds the response rule.
	 * 
	 * @param transformRule the tr
	 * 
	 * @return the transform response
	 */
	public Response applyResponseRule(TransformRule transformRule) {
		Response response = TransformFactory.createTransform(transformRule);
		
		if(transformRule.getContentType() != null)
			responseMap.put(transformRule.getContentType(), response);
		
		if(transformRule.isDefaultResponse())
			defaultResponse = response;
		
		if(defaultResponse == null && transformRule.getContentType() == null)
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
	public Response applyResponseRule(DispatchResponseRule dispatchResponseRule) {
		Response response = new DispatchResponse(dispatchResponseRule);
		
		if(dispatchResponseRule.getContentType() != null)
			responseMap.put(dispatchResponseRule.getContentType(), response);
		
		if(dispatchResponseRule.isDefaultResponse())
			defaultResponse = response;
		
		if(defaultResponse == null && dispatchResponseRule.getContentType() == null)
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
	public Response applyResponseRule(RedirectResponseRule redirectResponseRule) {
		Response response = new RedirectResponse(redirectResponseRule);

		if(redirectResponseRule.getContentType() != null)
			responseMap.put(redirectResponseRule.getContentType(), response);
		
		if(redirectResponseRule.getDefaultResponse() == Boolean.TRUE)
			defaultResponse = response;
		
		if(defaultResponse == null && redirectResponseRule.getContentType() == null)
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
	public Response applyResponseRule(ForwardResponseRule forwardResponseRule) {
		Response response = new ForwardResponse(forwardResponseRule);

		if(forwardResponseRule.getContentType() != null)
			responseMap.put(forwardResponseRule.getContentType(), response);
		
		if(forwardResponseRule.isDefaultResponse())
			defaultResponse = response;
		
		if(defaultResponse == null && forwardResponseRule.getContentType() == null)
			defaultResponse = response;
		
		return response;
	}

	/**
	 * Adds the response.
	 * 
	 * @param resonse the resonse
	 */
	public void addResponse(Response response) {
		responseMap.put(response.getContentType(), response);
	}
	
	public static ResponseByContentTypeRule newInstance(String exceptionType) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
		if(exceptionType != null)
			rbctr.setExceptionType(exceptionType);
		
		return rbctr;
	}
	
}
