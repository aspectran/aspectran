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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.type.ResponseType;

/**
 * JSP or other web resource integration.
 * 
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class DispatchResponse implements Response {

	private final Logger logger = LoggerFactory.getLogger(DispatchResponse.class);

	private final boolean debugEnabled = logger.isDebugEnabled();

	private final DispatchResponseRule dispatchResponseRule;
	
	private ViewDispatcher viewDispatcher;

	/**
	 * Instantiates a new dispatch response.
	 * 
	 * @param dispatchResponseRule the dispatch response rule
	 */
	public DispatchResponse(DispatchResponseRule dispatchResponseRule) {
		this.dispatchResponseRule = dispatchResponseRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.response.Responsible#response(org.jhlabs.translets.action.Translet)
	 */
	public void response(Activity activity) throws ResponseException {
		try {
			if(debugEnabled) {
				logger.debug("response " + dispatchResponseRule);
			}

			determineViewDispatcher(activity);
			
			if(viewDispatcher != null) {
				viewDispatcher.dispatch(activity, dispatchResponseRule);
			}
		} catch(Exception e) {
			throw new DispatchResponseException("Dispatch Response error: " + dispatchResponseRule, e);
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
	
	private void determineViewDispatcher(Activity activity) {
		if(viewDispatcher == null) {
			synchronized(this) {
				if(viewDispatcher == null) {
					String viewDispatcherName = activity.getResponseSetting(ResponseRule.VIEW_DISPATCHER_SETTING_NAME);
					viewDispatcher = (ViewDispatcher)activity.getBean(viewDispatcherName);
				}
			}
		}
		
		if(viewDispatcher == null)
			throw new RuntimeException("View Dispatcher is not defined.");
	}
	
}
