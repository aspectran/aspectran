/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.activity;

import java.lang.reflect.Constructor;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.translet.TransletInstantiationException;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public abstract class AbstractActivity {

	private ApplicationAdapter applicationAdapter;
	
	/** The request adapter. */
	private RequestAdapter requestAdapter;

	/** The response adapter. */
	private ResponseAdapter responseAdapter;

	/** The session adapter. */
	private SessionAdapter sessionAdapter;

	protected AbstractActivity(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}


	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getSessionAdapter()
	 */
	public SessionAdapter getSessionAdapter() {
		return sessionAdapter;
	}
	
	/**
	 * Sets the session adapter.
	 *
	 * @param sessionAdapter the new session adapter
	 */
	protected void setSessionAdapter(SessionAdapter sessionAdapter) {
		this.sessionAdapter = sessionAdapter;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getRequestAdapter()
	 */
	public RequestAdapter getRequestAdapter() {
		return requestAdapter;
	}
	
	/**
	 * Sets the request adapter.
	 *
	 * @param requestAdapter the new request adapter
	 */
	protected void setRequestAdapter(RequestAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getResponseAdapter()
	 */
	public ResponseAdapter getResponseAdapter() {
		return responseAdapter;
	}

	/**
	 * Sets the response adapter.
	 *
	 * @param responseAdapter the new response adapter
	 */
	protected void setResponseAdapter(ResponseAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}
	
	protected Translet newTranslet(Class<? extends CoreTranslet> transletImplementClass) {
		//create a custom translet instance
		try {
			Constructor<?> transletImplementConstructor = transletImplementClass.getConstructor(Activity.class);
			Object[] args = new Object[] { this };
			
			return (Translet)transletImplementConstructor.newInstance(args);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletImplementClass, e);
		}
	}

	
}
