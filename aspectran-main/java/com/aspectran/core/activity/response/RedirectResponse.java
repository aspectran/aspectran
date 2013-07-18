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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.SuperTranslet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.rule.RedirectResponseRule;
import com.aspectran.core.type.ResponseType;

/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class RedirectResponse implements Responsible {
	
	private final Log log = LogFactory.getLog(RedirectResponse.class);

	private final boolean debugEnabled = log.isDebugEnabled();

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
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(AspectranActivity activity) throws ResponseException {
		try {
			ResponseAdapter responseAdapter = activity.getResponseAdapter();

			String outputEncoding = redirectResponseRule.getCharacterEncoding();

			if(outputEncoding == null)
				responseAdapter.setCharacterEncoding(outputEncoding);

			String url = responseAdapter.redirect(activity, redirectResponseRule);
			
			if(debugEnabled) {
				log.debug("Redirect url: " + url);
				log.debug("Redirect response ok.");
			}
		} catch(Exception e) {
			throw new ResponseException("Redirect response error: " + redirectResponseRule, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getResponseType()
	 */
	public ResponseType getResponseType() {
		return RedirectResponseRule.RESPONSE_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getId()
	 */
	public String getId() {
		if(redirectResponseRule == null)
			return null;

		return redirectResponseRule.getId();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getContentType()
	 */
	public String getContentType() {
		if(redirectResponseRule == null)
			return null;

		return redirectResponseRule.getContentType();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
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
