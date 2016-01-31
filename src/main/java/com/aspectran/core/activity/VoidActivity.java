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
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.type.JoinpointScopeType;

/**
 * The Class VoidActivity
 * 
 * <p>Created: 2008. 04. 28 AM 12:48:48</p>
 */
public final class VoidActivity extends CoreActivity implements Activity {
	
	/**
	 * Instantiates a new void activity.
	 *
	 * @param context the context
	 */
	public VoidActivity(ActivityContext context) {
		super(context);
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
	public <T extends Activity> T newActivity() {
		VoidActivity activity = new VoidActivity(getActivityContext());
		return (T)activity;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getRequestAdapter()
	 */
	public RequestAdapter getRequestAdapter() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getResponseAdapter()
	 */
	public ResponseAdapter getResponseAdapter() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getSessionAdapter()
	 */
	public SessionAdapter getSessionAdapter() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getTransletName()
	 */
	public String getTransletName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#perform()
	 */
	public void perform() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#performWithoutResponse()
	 */
	@Override
	public void performWithoutResponse() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#finish()
	 */
	public void finish() {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getForwardTransletName()
	 */
	public String getForwardTransletName() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#response(com.aspectran.core.activity.response.Response)
	 */
	public void response(Response res) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#responseByContentType(java.util.List)
	 */
	public void responseByContentType(List<ExceptionHandlingRule> exceptionHandlingRuleList) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getResponse()
	 */
	public Response getResponse() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getRequestScope()
	 */
	public Scope getRequestScope() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#setRequestScope(com.aspectran.core.context.bean.scope.Scope)
	 */
	public void setRequestScope(Scope requestScope) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AbstractActivity#getCurrentJoinpointScope()
	 */
	public JoinpointScopeType getCurrentJoinpointScope() {
		return null;
	}

}
