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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.var.rule.AspectAdviceRule;

/**
 * <p>Created: 2008. 04. 28 오전 12:48:48</p>
 */
public class VoidActivityImpl extends AbstractCoreActivity implements CoreActivity {
	
	public VoidActivityImpl(ActivityContext context) {
		super(context);
	}
	
	public void init(String transletName) throws CoreActivityException {
		setTransletInterfaceClass(CoreTranslet.class);
		setTransletImplementClass(CoreTransletImpl.class);

	}
	
	protected void request(CoreTranslet translet) throws RequestException {
	}
	
	public CoreActivity newCoreActivity() {
		VoidActivityImpl voidActivity = new VoidActivityImpl(getActivityContext());
		return voidActivity;
	}

	@Override
	public RequestAdapter getRequestAdapter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResponseAdapter getResponseAdapter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SessionAdapter getSessionAdapter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void run() throws CoreActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void runWithoutResponse() throws CoreActivityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void request() throws RequestException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessResult process() throws CoreActivityException {
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
	public void responseByContentType(
			List<AspectAdviceRule> aspectAdviceRuleList)
			throws CoreActivityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Responsible getResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTransletName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scope getRequestScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRequestScope(Scope requestScope) {
		// TODO Auto-generated method stub
		
	}

}
