/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity;

import java.lang.reflect.Constructor;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.translet.TransletInstantiationException;

/**
 * The Class AbstractActivity.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public abstract class AbstractActivity {

	private ApplicationAdapter applicationAdapter;

	/** The session adapter. */
	private SessionAdapter sessionAdapter;
	
	/** The request adapter. */
	private RequestAdapter requestAdapter;

	/** The response adapter. */
	private ResponseAdapter responseAdapter;
	
	/** The request scope. */
	private Scope requestScope;
	
	private JoinpointScopeType currentJoinpointScope = JoinpointScopeType.TRANSLET;
	
	/** The translet interface class. */
	private Class<? extends Translet> transletInterfaceClass;
	
	/** The translet instance class. */
	private Class<? extends CoreTranslet> transletImplementClass;

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

	
	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	/**
	 * Sets the translet interface class.
	 *
	 * @param transletInterfaceClass the new translet interface class
	 */
	protected void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	/**
	 * Gets the translet instance class.
	 *
	 * @return the translet instance class
	 */
	public Class<? extends CoreTranslet> getTransletImplementClass() {
		return transletImplementClass;
	}

	/**
	 * Sets the translet instance class.
	 *
	 * @param transletInstanceClass the new translet instance class
	 */
	protected void setTransletImplementClass(Class<? extends CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}
	
	protected Translet newTranslet(Activity activity) {
		if(this.transletInterfaceClass == null)
			this.transletInterfaceClass = Translet.class;
		
		if(this.transletImplementClass == null) {
			this.transletImplementClass = CoreTranslet.class;
			return new CoreTranslet(activity);
		}
		
		//create a custom translet instance
		try {
			Constructor<?> transletImplementConstructor = transletImplementClass.getConstructor(Activity.class);
			Object[] args = new Object[] { activity };
			
			return (Translet)transletImplementConstructor.newInstance(args);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletImplementClass, e);
		}
	}
	
	public Scope getRequestScope() {
		return requestScope;
	}

	public void setRequestScope(Scope requestScope) {
		this.requestScope = requestScope;
	}

	public JoinpointScopeType getCurrentJoinpointScope() {
		return currentJoinpointScope;
	}

	protected void setCurrentJoinpointScope(JoinpointScopeType joinpointScope) {
		this.currentJoinpointScope = joinpointScope;
	}
	
}
