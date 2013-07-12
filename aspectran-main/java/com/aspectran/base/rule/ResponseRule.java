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

import com.aspectran.core.activity.response.DispatchResponse;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.transform.AbstractTransform;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:48:09
 * </p>
 */
public class ResponseRule extends AbstractResponseRule {

	/** The Constant DEFAULT_ID. */
	public static final String DEFAULT_ID = "[default]";

	/** The character encoding. */
	private String characterEncoding;
	
	/** The default response id. */
	private String defaultResponseId;
	
	/** The default content type. */
	private String defaultContentType;
	
	/**
	 * Instantiates a new response rule.
	 */
	public ResponseRule() {
		super();
	}
	
	/**
	 * Instantiates a new response rule.
	 *
	 * @param drr the drr
	 */
	public ResponseRule(DefaultResponseRule drr) {
		super();
		
		characterEncoding = drr.getCharacterEncoding();
		defaultContentType = drr.getDefaultContentType();
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
	 * Gets the default content type.
	 *
	 * @return the default content type
	 */
	public String getDefaultContentType() {
		return defaultContentType;
	}

	/**
	 * Sets the default content type.
	 *
	 * @param defaultContentType the new default content type
	 */
	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}
	
	/**
	 * Instantiates a new response rule.
	 * 
	 * @param responseMap the response map
	 */
	public ResponseRule(ResponseMap responseMap) {
		super(responseMap);
	}
	
	/**
	 * Gets the default response id.
	 * 
	 * @return the defaultResponseId
	 */
	public String getDefaultResponseId() {
		return defaultResponseId;
	}

	/**
	 * Sets the default response id.
	 * 
	 * @param defaultResponseId the defaultResponseId to set
	 */
	public void setDefaultResponseId(String defaultResponseId) {
		this.defaultResponseId = defaultResponseId;
	}

	/**
	 * Adds the response rule.
	 * 
	 * @param tr the tr
	 * 
	 * @return the transform response
	 */
	public AbstractTransform addResponse(TransformRule tr) {
		if(tr.getId() == null)
			tr.setId(DEFAULT_ID);
		
		if(tr.getCharacterEncoding() == null)
			tr.setCharacterEncoding(characterEncoding);

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
		if(drr.getId() == null)
			drr.setId(DEFAULT_ID);

		if(drr.getCharacterEncoding() == null)
			drr.setCharacterEncoding(characterEncoding);
		
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
		if(rrr.getId() == null)
			rrr.setId(DEFAULT_ID);

		if(rrr.getCharacterEncoding() == null)
			rrr.setCharacterEncoding(characterEncoding);
		
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
		if(frr.getId() == null)
			frr.setId(DEFAULT_ID);
		
		return super.addResponse(frr);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{characterEncoding=").append(characterEncoding);
		sb.append(", defaultContentType=").append(defaultContentType);
		sb.append(", defaultResponseId=").append(defaultResponseId);
		sb.append("} ");
		sb.append(getResponseMap().toString());
		
		return sb.toString();
	}
}
