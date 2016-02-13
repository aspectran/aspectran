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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class RedirectResponse.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class RedirectResponse implements Response {
	
	private final Log log = LogFactory.getLog(RedirectResponse.class);

	private final boolean debugEnabled = log.isDebugEnabled();

	private final RedirectResponseRule redirectResponseRule;

	/**
	 * Instantiates a new RedirectResponse.
	 * 
	 * @param redirectResponseRule the redirect response rule
	 */
	public RedirectResponse(RedirectResponseRule redirectResponseRule) {
		this.redirectResponseRule = redirectResponseRule;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#response(com.aspectran.core.activity.Activity)
	 */
	public void response(Activity activity) throws ResponseException {
		ResponseAdapter responseAdapter = activity.getResponseAdapter();
		if(responseAdapter == null)
			return;
		
		if(debugEnabled) {
			log.debug("response " + redirectResponseRule);
		}
		
		try {
			String characterEncoding = redirectResponseRule.getCharacterEncoding();
			if(characterEncoding != null)
				responseAdapter.setCharacterEncoding(characterEncoding);
			
			responseAdapter.redirect(redirectResponseRule, activity);
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

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.response.Response#replicate()
	 */
	public Response replicate() {
		RedirectResponseRule rrr = redirectResponseRule.replicate();
		Response response = new RedirectResponse(rrr);
		return response;
	}
	
	/**
	 * Gets the redirect response rule.
	 * 
	 * @return the redirect response rule
	 */
	public RedirectResponseRule getRedirectResponseRule() {
		return redirectResponseRule;
	}

	@Override
	public String toString() {
		return redirectResponseRule.toString();
	}

}
