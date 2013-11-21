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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.context.rule.ability.ResponseAddable;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class ResponseByContentTypeRule implements ResponseAddable {

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
	 * @param tr the tr
	 * 
	 * @return the transform response
	 */
	public AbstractTransform addResponse(TransformRule tr) {
		AbstractTransform transformResponse = AbstractTransform.createTransformer(tr);
		
		responseMap.put(tr.getContentType(), transformResponse);
		
		return transformResponse;
	}

	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse addResponse(DispatchResponseRule drr) {
		DispatchResponse dispatchResponse = new DispatchResponse(drr);
		
		responseMap.put(drr.getContentType(), dispatchResponse);
		
		return dispatchResponse;
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse addResponse(RedirectResponseRule rrr) {
		RedirectResponse redirectResponse = new RedirectResponse(rrr);

		responseMap.put(rrr.getContentType(), redirectResponse);
		
		return redirectResponse;
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public ForwardResponse addResponse(ForwardResponseRule frr) {
		ForwardResponse forwardResponse = new ForwardResponse(frr);

		responseMap.put(frr.getContentType(), forwardResponse);
		
		return forwardResponse;
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
	 * @param tr the new default response rule
	 * 
	 * @return the transform response
	 */
	public AbstractTransform setDefaultResponse(TransformRule tr) {
		AbstractTransform transformResponse = AbstractTransform.createTransformer(tr);
		
		this.defaultResponse = transformResponse;
		
		return transformResponse;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param drr the new default response rule
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse setDefaultResponse(DispatchResponseRule drr) {
		DispatchResponse dispatchResponse = new DispatchResponse(drr);
		
		this.defaultResponse = dispatchResponse;
		
		return dispatchResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param rrr the new default response rule
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse setDefaultResponse(RedirectResponseRule rrr) {
		RedirectResponse redirectResponse = new RedirectResponse(rrr);

		this.defaultResponse = redirectResponse;
		
		return redirectResponse;
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param frr the new default response rule
	 * 
	 * @return the forward response
	 */
	public ForwardResponse setDefaultResponse(ForwardResponseRule frr) {
		ForwardResponse forwardResponse = new ForwardResponse(frr);

		this.defaultResponse = forwardResponse;
		
		return forwardResponse;
	}

}
