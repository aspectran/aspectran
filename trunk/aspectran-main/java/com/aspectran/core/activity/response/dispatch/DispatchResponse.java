/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.activity.response.dispatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.rule.DispatchResponseRule;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.type.ResponseType;

/**
 * JSP or other web resource integration.
 * 
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class DispatchResponse implements Responsible {

	private final Log log = LogFactory.getLog(DispatchResponse.class);

	private final boolean debugEnabled = log.isDebugEnabled();

	private final DispatchResponseRule dispatchResponseRule;
	
	private ViewDispatcher viewDispatcher;

	/**
	 * Instantiates a new dispatch response.
	 * 
	 * @param dispatchResponseRule the dispatch response rule
	 */
	public DispatchResponse(DispatchResponseRule dispatchResponseRule) {
		this.dispatchResponseRule = dispatchResponseRule;
		this.viewDispatcher = dispatchResponseRule.getViewDispatcher();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(AspectranActivity activity) throws ResponseException {
		try {
			if(viewDispatcher == null) {
				String viewDispatcherClassType  = (String)activity.getResponseSetting(ResponseRule.VIEW_DISPATCHER_SETTING);
				
			}
			
			if(viewDispatcher != null) {
				viewDispatcher.dispatch(activity, dispatchResponseRule);
			}

			if(debugEnabled) {
				log.debug("Dispatch view template '" + dispatchResponseRule.getTemplate() + "'");
				log.debug("Dispatch response ok.");
			}
		} catch(Exception e) {
			throw new DispatchResponseException("Dispatch response error: " + dispatchResponseRule, e);
		}
	}

	/**
	 * Gets the dispatch response rule.
	 * 
	 * @return the dispatch response rule
	 */
	public DispatchResponseRule getDispatchResponseRule() {
		return dispatchResponseRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getId()
	 */
	public String getId() {
		return dispatchResponseRule.getId();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getContentType()
	 */
	public String getContentType() {
		return dispatchResponseRule.getContentType();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getResponseType()
	 */
	public ResponseType getResponseType() {
		return DispatchResponseRule.RESPONSE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#getActionList()
	 */
	public ActionList getActionList() {
		return dispatchResponseRule.getActionList();
	}
}
