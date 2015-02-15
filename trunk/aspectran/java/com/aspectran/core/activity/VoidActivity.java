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

import java.util.List;

import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.AspectAdviceRule;

/**
 * <p>Created: 2008. 04. 28 오전 12:48:48</p>
 */
public final class VoidActivity extends CoreActivity implements Activity {
	
	public VoidActivity(ActivityContext context) {
		super(context);
	}
	
	public void ready(String transletName) throws ActivityException {
		newTranslet();
	}
	
	protected void request(Translet translet) throws RequestException {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Activity> T newActivity() {
		VoidActivity activity = new VoidActivity(getActivityContext());
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
	public SessionAdapter getSessionAdapter() {
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
	public String getForwardTransletName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void response(Responsible res) throws ResponseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void responseByContentType(List<AspectAdviceRule> aspectAdviceRuleList) throws ActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Responsible getResponse() {
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

}
