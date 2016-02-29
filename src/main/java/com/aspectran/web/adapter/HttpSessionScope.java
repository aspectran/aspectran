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

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.SessionScope;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class HttpSessionScope.
 *
 * @author Juho Jeong
 */
public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener {

	private static final Log log = LogFactory.getLog(HttpSessionScope.class);
	
	private SessionAdapter sessionAdapter;
	
	private SessionScopeAdvisor advisor;
	
	/**
	 * Instantiates a new HttpSessionScope.
	 */
	public HttpSessionScope() {
	}

	/**
	 * Instantiates a new HttpSessionScope.
	 *
	 * @param sessionAdapter the session adapter
	 * @param advisor the session scope advisor
	 */
	public HttpSessionScope(SessionAdapter sessionAdapter, SessionScopeAdvisor advisor) {
		this.sessionAdapter = sessionAdapter;
		this.advisor = advisor;
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		if(log.isDebugEnabled())
			log.debug("New HttpSessionScope bound in session " + sessionAdapter);
		
		if(advisor != null)
			advisor.executeBeforeAdvice();
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		sessionAdapter.release();

		if(log.isDebugEnabled())
			log.debug("HttpSessionScope removed from session " + sessionAdapter);
		
		if(advisor != null)
			advisor.executeAfterAdvice();
		
		this.destroy();
	}
	
}
