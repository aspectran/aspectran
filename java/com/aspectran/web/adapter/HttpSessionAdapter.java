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
package com.aspectran.web.adapter;

import javax.servlet.http.HttpSession;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class HttpSessionAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter implements SessionAdapter {
	
	public static final String SESSION_SCOPE_ATTRIBUTE = HttpSessionScope.class.getName();
	
	/**
	 * Instantiates a new http session adapter.
	 *
	 * @param session the session
	 */
	public HttpSessionAdapter(HttpSession session) {
		super(session);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)((HttpSession)adaptee).getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		((HttpSession)adaptee).setAttribute(name, value);
	}

	public synchronized Scope getScope() {
		Scope scope = (SessionScope)getAttribute(SESSION_SCOPE_ATTRIBUTE);

		if(scope == null) {
			scope = new HttpSessionScope();
			setAttribute(SESSION_SCOPE_ATTRIBUTE, scope);
		}
		
		return scope;
	}

}
