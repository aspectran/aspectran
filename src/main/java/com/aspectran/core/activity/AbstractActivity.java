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
package com.aspectran.core.activity;

import java.lang.reflect.Constructor;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.translet.TransletInstantiationException;

/**
 * The Class AbstractActivity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public abstract class AbstractActivity {

	private ApplicationAdapter applicationAdapter;

	private SessionAdapter sessionAdapter;
	
	private RequestAdapter requestAdapter;

	private ResponseAdapter responseAdapter;
	
	private Scope requestScope;
	
	private JoinpointScopeType currentJoinpointScope = JoinpointScopeType.TRANSLET;
	
	private Class<? extends Translet> transletInterfaceClass;
	
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

	/**
	 * Gets the session adapter.
	 *
	 * @return the session adapter
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

	/**
	 * Gets the request adapter.
	 *
	 * @return the request adapter
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
	
	/**
	 * Gets the response adapter.
	 *
	 * @return the response adapter
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
	 * Gets the translet implement class.
	 *
	 * @return the translet implement class
	 */
	public Class<? extends CoreTranslet> getTransletImplementationClass() {
		return transletImplementClass;
	}

	/**
	 * Sets the translet implement class.
	 *
	 * @param transletImplementClass the new translet implement class
	 */
	protected void setTransletImplementationClass(Class<? extends CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}
	
	/**
	 * Create a new translet.
	 *
	 * @param activity the activity
	 * @return the translet
	 */
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
	
	/**
	 * Gets the request scope.
	 *
	 * @return the request scope
	 */
	public synchronized Scope getRequestScope() {
		if(requestScope == null) {
			requestScope = new RequestScope();
		}
		return requestScope;
	}

	/**
	 * Sets the request scope.
	 *
	 * @param requestScope the new request scope
	 */
	public void setRequestScope(Scope requestScope) {
		this.requestScope = requestScope;
	}

	/**
	 * Gets the current joinpoint scope.
	 *
	 * @return the current joinpoint scope
	 */
	public JoinpointScopeType getCurrentJoinpointScope() {
		return currentJoinpointScope;
	}

	/**
	 * Sets the current joinpoint scope.
	 *
	 * @param joinpointScope the new current joinpoint scope
	 */
	protected void setCurrentJoinpointScope(JoinpointScopeType joinpointScope) {
		this.currentJoinpointScope = joinpointScope;
	}
	
}
