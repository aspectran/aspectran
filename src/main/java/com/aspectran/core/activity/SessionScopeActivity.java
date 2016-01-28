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

import java.util.List;

import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.type.JoinpointScopeType;

/**
 * The Class SessionScopeActivity.
 *
 * @since 1.5.0
 */
public final class SessionScopeActivity extends CoreActivity implements Activity {

	/**
	 * Instantiates a new session scope activity.
	 *
	 * @param context the context
	 * @param sessionAdapter the session adapter
	 */
	public SessionScopeActivity(ActivityContext context, SessionAdapter sessionAdapter) {
		super(context);
		setSessionAdapter(sessionAdapter);
		newTranslet();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#ready(java.lang.String)
	 */
	public void ready(String transletName) {
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#request(com.aspectran.core.activity.Translet)
	 */
	protected void request(Translet translet) {
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#newActivity()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Activity> T newActivity() {
		SessionScopeActivity activity = new SessionScopeActivity(getActivityContext(), getSessionAdapter());
		return (T)activity;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getRequestAdapter()
	 */
	@Override
	public RequestAdapter getRequestAdapter() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getResponseAdapter()
	 */
	@Override
	public ResponseAdapter getResponseAdapter() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getTransletName()
	 */
	@Override
	public String getTransletName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#perform()
	 */
	@Override
	public void perform() throws ActivityException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#performWithoutResponse()
	 */
	@Override
	public void performWithoutResponse() throws ActivityException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#finish()
	 */
	@Override
	public void finish() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getForwardTransletName()
	 */
	@Override
	public String getForwardTransletName() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#response(com.aspectran.core.activity.response.Response)
	 */
	@Override
	public void response(Response res) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#responseByContentType(java.util.List)
	 */
	@Override
	public void responseByContentType(List<ExceptionHandlingRule> exceptionHandlingRuleList) throws ActivityException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getResponse()
	 */
	@Override
	public Response getResponse() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getRequestScope()
	 */
	@Override
	public Scope getRequestScope() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#setRequestScope(com.aspectran.core.context.bean.scope.Scope)
	 */
	@Override
	public void setRequestScope(Scope requestScope) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getCurrentJoinpointScope()
	 */
	public JoinpointScopeType getCurrentJoinpointScope() {
		return JoinpointScopeType.SESSION;
	}

}
