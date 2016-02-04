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

	@Override
	public void ready(String transletName) {
	}

	@Override
	protected void request(Translet translet) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		SessionScopeActivity activity = new SessionScopeActivity(getActivityContext(), getSessionAdapter());
		return (T)activity;
	}

	@Override
	public RequestAdapter getRequestAdapter() {
		return null;
	}

	@Override
	public ResponseAdapter getResponseAdapter() {
		return null;
	}

	@Override
	public String getTransletName() {
		return null;
	}

	@Override
	public void perform() throws ActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void performWithoutResponse() throws ActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finish() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getForwardTransletName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void response(Response res) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void responseByContentType(List<ExceptionHandlingRule> exceptionHandlingRuleList) throws ActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Response getResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Scope getRequestScope() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRequestScope(Scope requestScope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public JoinpointScopeType getCurrentJoinpointScope() {
		return JoinpointScopeType.SESSION;
	}

}
