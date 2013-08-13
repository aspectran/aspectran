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

import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.rule.ability.ResponseAddable;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class ResponseByContentTypeRule extends AbstractResponseRule implements ResponseAddable {

	private String exceptionType;
	
	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param tr the new default response rule
	 * 
	 * @return the transform response
	 */
	public AbstractTransform setDefaultResponse(TransformRule tr) {
		tr.setId(ResponseRule.DEFAULT_ID);
		return super.addResponse(tr);
	}

	/**
	 * Sets the default response rule.
	 * 
	 * @param drr the new default response rule
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse setDefaultResponse(DispatchResponseRule drr) {
		drr.setId(ResponseRule.DEFAULT_ID);
		return super.addResponse(drr);
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param rrr the new default response rule
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse setDefaultResponse(RedirectResponseRule rrr) {
		rrr.setId(ResponseRule.DEFAULT_ID);
		return super.addResponse(rrr);
	}
	
	/**
	 * Sets the default response rule.
	 * 
	 * @param frr the new default response rule
	 * 
	 * @return the forward response
	 */
	public ForwardResponse setDefaultResponse(ForwardResponseRule frr) {
		frr.setId(ResponseRule.DEFAULT_ID);
		return super.addResponse(frr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param tr the tr
	 * 
	 * @return the transform response
	 */
	public AbstractTransform addResponse(TransformRule tr) {
		if(tr.getContentType() == null)
			return setDefaultResponse(tr);

		tr.setId(tr.getContentType().toString());
		
		return super.addResponse(tr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public DispatchResponse addResponse(DispatchResponseRule drr) {
		if(drr.getContentType() == null)
			return setDefaultResponse(drr);
		
		drr.setId(drr.getContentType().toString());

		
		return super.addResponse(drr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public RedirectResponse addResponse(RedirectResponseRule rrr) {
		if(rrr.getContentType() == null)
			return setDefaultResponse(rrr);
			
		rrr.setId(rrr.getContentType().toString());
		
		return super.addResponse(rrr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public ForwardResponse addResponse(ForwardResponseRule frr) {
		if(frr.getContentType() == null)
			return setDefaultResponse(frr);
		
		frr.setId(frr.getContentType().toString());

		return super.addResponse(frr);
	}
}
