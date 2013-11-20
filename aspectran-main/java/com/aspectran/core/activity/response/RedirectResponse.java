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
package com.aspectran.core.activity.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.rule.RedirectResponseRule;
import com.aspectran.core.type.ResponseType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class RedirectResponse implements Responsible {
	
	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(RedirectResponse.class);

	/** The debug enabled. */
	private final boolean debugEnabled = logger.isDebugEnabled();

	/** The redirect response rule. */
	private final RedirectResponseRule redirectResponseRule;

	/**
	 * Instantiates a new redirect response.
	 * 
	 * @param redirectResponseRule the redirect response rule
	 */
	public RedirectResponse(RedirectResponseRule redirectResponseRule) {
		this.redirectResponseRule = redirectResponseRule;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#response(com.aspectran.core.activity.CoreActivity)
	 */
	public void response(CoreActivity activity) throws ResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		
		if(responseAdapter == null)
			return;
		
		if(debugEnabled) {
			logger.debug("response " + redirectResponseRule);
		}
		
		try {
			String outputEncoding = redirectResponseRule.getCharacterEncoding();

			if(outputEncoding != null)
				responseAdapter.setCharacterEncoding(outputEncoding);
			
			responseAdapter.redirect(activity, redirectResponseRule);
		} catch(Exception e) {
			throw new ResponseException("Redirect response error: " + redirectResponseRule, e);
		}
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getResponseType()
	 */
	public ResponseType getResponseType() {
		return RedirectResponseRule.RESPONSE_TYPE;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getId()
	 */
	public String getId() {
		if(redirectResponseRule == null)
			return null;

		return redirectResponseRule.getId();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getContentType()
	 */
	public String getContentType() {
		if(redirectResponseRule == null)
			return null;

		return redirectResponseRule.getContentType();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return redirectResponseRule.getActionList();
	}

	/**
	 * Gets the redirect response rule.
	 * 
	 * @return the redirect response rule
	 */
	public RedirectResponseRule getRedirectResponseRule() {
		return redirectResponseRule;
	}
}
