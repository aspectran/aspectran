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
package com.aspectran.base.rule;

import com.aspectran.base.type.ResponseType;
import com.aspectran.core.activity.response.DispatchResponse;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.transform.AbstractTransform;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:48:09
 * </p>
 */
public abstract class AbstractResponseRule {

	private ResponseMap responseMap;
	
	/**
	 * Instantiates a new response rule.
	 */
	public AbstractResponseRule() {
		this.responseMap = new ResponseMap();
	}
	
	/**
	 * Instantiates a new response rule.
	 * 
	 * @param responseMap the response map
	 */
	public AbstractResponseRule(ResponseMap responseMap) {
		this.responseMap = responseMap;
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
		
		responseMap.putResponse(transformResponse);
		
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
		
		responseMap.putResponse(dispatchResponse);
		
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

		responseMap.putResponse(redirectResponse);
		
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

		responseMap.putResponse(forwardResponse);
		
		return forwardResponse;
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

	/**
	 * Adds the response.
	 * 
	 * @param resonse the resonse
	 */
	public void addResponse(Responsible response) {
		responseMap.putResponse(response);
	}
	
	/**
	 * Sets the default response.
	 * 
	 * @param response the new default response
	 */
	public void setDefaultResponse(Responsible response) {
		ResponseType type = response.getResponseType();
		
		if(type == ResponseType.TRANSFORM) {
			AbstractTransform tr = (AbstractTransform)response;
			TransformRule rule = tr.getTransformRule();
			rule.setId(ResponseRule.DEFAULT_ID);
		} else if(type == ResponseType.DISPATCH) {
			DispatchResponse dr = (DispatchResponse)response;
			DispatchResponseRule drr = dr.getDispatchResponseRule();
			drr.setId(ResponseRule.DEFAULT_ID);
		} else if(type == ResponseType.FORWARD) {
			ForwardResponse fr = (ForwardResponse)response;
			ForwardResponseRule frr = fr.getForwardResponseRule();
			frr.setId(ResponseRule.DEFAULT_ID);
		} else if(type == ResponseType.REDIRECT) {
			RedirectResponse rr = (RedirectResponse)response;
			RedirectResponseRule rrr = rr.getRedirectResponseRule();
			rrr.setId(ResponseRule.DEFAULT_ID);
		}

		addResponse(response);
	}
}
