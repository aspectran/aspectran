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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class HttpSessionAdapter.
 * 
 * @since 2011. 3. 13.
 * @author Juho Jeong
 */
public class HttpSessionAdapter extends AbstractSessionAdapter {
	
	private static final String SESSION_SCOPE_ATTRIBUTE_NAME = HttpSessionScope.class.getName() + ".SESSION_SCOPE";
	
	private volatile SessionScope scope;
	
	private ActivityContext context;
	
	/**
	 * Instantiates a new HttpSessionAdapter.
	 *
	 * @param request the HTTP request
	 * @param context the current ActivityContext
	 */
	public HttpSessionAdapter(HttpServletRequest request, ActivityContext context) {
		super(request);
		this.context = context;
		
		if(getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME) == null) {
			newHttpSessionScope(false);
		}
	}

	@Override
	public String getId() {
		HttpSession session = getSession(false);
		if(session == null)
			return null;

		return session.getId();
	}

	@Override
	public long getCreationTime() {
		HttpSession session = getSession(false);
		if(session == null)
			return -1L;
		
		return session.getCreationTime();
	}

	@Override
	public long getLastAccessedTime() {
		HttpSession session = getSession(false);
		if(session == null)
			return -1L;
		
		return session.getLastAccessedTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		HttpSession session = getSession(false);
		if(session == null)
			return 0;
		
		return session.getMaxInactiveInterval();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		HttpSession session = getSession(false);
		if(session == null)
			return null;
		
		return session.getAttributeNames();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		HttpSession session = getSession(false);
		if(session == null)
			return null;
		
		return (T)session.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		if(value != null) {
			HttpSession session = getSession(true);
			session.setAttribute(name, value);
		} else {
			HttpSession session = getSession(false);
			if(session != null) {
				session.removeAttribute(name);
			}
		}
	}

	@Override
	public void invalidate() {
		HttpSession session = getSession(false);
		if(session != null) {
			session.invalidate();
		}
	}

	public HttpSession getSession(boolean create) {
		if(adaptee == null) {
			throw new IllegalStateException("Session has been expired or not yet initialized.");
		}
		return ((HttpServletRequest)adaptee).getSession(create);
	}

	@Override
	public SessionScope getSessionScope() {
		if(scope == null) {
			synchronized(this) {
				scope = getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
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
			setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, scope);
		}
		
		return scope;
	}

}
