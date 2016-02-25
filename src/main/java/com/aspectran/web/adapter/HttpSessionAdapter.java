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
package com.aspectran.web.adapter;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class HttpSessionAdapter.
 * 
 * @since 2011. 3. 13.
 * @author Juho Jeong
 */
public class HttpSessionAdapter extends AbstractSessionAdapter implements SessionAdapter {
	
	public static final String SESSION_SCOPE_ATTRIBUTE = HttpSessionScope.class.getName();
	
	private volatile SessionScope scope;
	
	private ActivityContext context;
	
	/**
	 * Instantiates a new HttpSessionAdapter.
	 *
	 * @param session the current HTTP session
	 * @param context the current ActivityContext
	 */
	public HttpSessionAdapter(HttpSession session, ActivityContext context) {
		super(session);
		this.context = context;
		
		if(getAttribute(SESSION_SCOPE_ATTRIBUTE) == null) {
			newHttpSessionScope(false);
		}
	}

	@Override
	public String getId() {
		checkSessionState();
		
		return ((HttpSession)adaptee).getId();
	}

	@Override
	public long getCreationTime() {
		checkSessionState();
		
		return ((HttpSession)adaptee).getCreationTime();
	}

	@Override
	public long getLastAccessedTime() {
		checkSessionState();
		
		return ((HttpSession)adaptee).getLastAccessedTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		checkSessionState();
		
		return ((HttpSession)adaptee).getMaxInactiveInterval();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		checkSessionState();
		
		return ((HttpSession)adaptee).getAttributeNames();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		checkSessionState();
		
		return (T)((HttpSession)adaptee).getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		checkSessionState();
		
		((HttpSession)adaptee).setAttribute(name, value);
	}

	@Override
	public void invalidate() {
		checkSessionState();
		
		((HttpSession)adaptee).invalidate();
	}

	private void checkSessionState() {
		if(adaptee == null) {
			throw new IllegalStateException("Session has been expired or not yet initialized.");
		}
	}

	@Override
	public SessionScope getSessionScope() {
		if(scope == null) {
			synchronized(this) {
				scope = getAttribute(SESSION_SCOPE_ATTRIBUTE);
				
				if(scope == null) {
					newHttpSessionScope(true);
				}
			}
		}
		
		return scope;
	}
	
	/**
	 * Return a new http session scope.
	 *
	 * @param force Whether to create a new session scope to force
	 * @return the session scope
	 */
	private SessionScope newHttpSessionScope(boolean force) {
		SessionScopeAdvisor advisor = SessionScopeAdvisor.newInstance(context, this);
		
		if(advisor != null || force) {
			scope = new HttpSessionScope(this, advisor);
			setAttribute(SESSION_SCOPE_ATTRIBUTE, scope);
		}
		
		return scope;
	}

}
