/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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

import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.base.adapter.RequestAdapter;
import com.aspectran.base.adapter.ResponseAdapter;
import com.aspectran.base.adapter.SessionAdapter;
import com.aspectran.base.context.ActivityContext;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.bean.registry.BeanRegistry;
import com.aspectran.core.bean.scope.RequestScope;
import com.aspectran.core.translet.ActivityTranslet;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public interface Activity {

	public RequestAdapter getRequestAdapter();
	
	public void setRequestAdapter(RequestAdapter requestAdapter);
	
	public ResponseAdapter getResponseAdapter();
	
	public void setResponseAdapter(ResponseAdapter responseAdapter);

	public SessionAdapter getSessionAdapter();
	
	public void setSessionAdapter(SessionAdapter sessionAdapter);
	
	public void request(String transletName) throws RequestException;
	
	public void process() throws ProcessException;
	
	public void process(boolean ignoreTicket) throws ProcessException;
	
	public void response() throws ResponseException;
	
	//public void translate(String path) throws RequestException, ProcessException, ResponseException;
	
	public String getEnforceableResponseId();
	
	public String getForwardTransletName();
	
	public void setForwardTransletName(String forwardingPath);
	
	public void responseEnd();
	
	public void response(Responsible res) throws ResponseException;
	
	public ActivityContext getContext();
	
	public Responsible getResponse(String responseId);
	
	public Responsible getResponse();
	
	public Object getBean(String id);
	
	public ActivityTranslet getActivityTranslet();
	
	public ApplicationAdapter getApplicationAdapter();
	
	public String getTransletName();
	
	public RequestScope getRequestScope();
	
	public void setRequestScope(RequestScope requestScope);
	
	public Activity newActivity();
	
	public BeanRegistry getBeanRegistry();
	
}
