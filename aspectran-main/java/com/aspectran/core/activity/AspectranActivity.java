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

import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.RequestScope;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public interface AspectranActivity {

	public RequestAdapter getRequestAdapter();
	
	public ResponseAdapter getResponseAdapter();
	
	public SessionAdapter getSessionAdapter();
	
	public Class<? extends SuperTranslet> getTransletInterfaceClass();
	
	public Class<? extends AbstractSuperTranslet> getTransletImplementClass();
	
	public SuperTranslet getSuperTranslet();
	
	public void run(String transletName) throws RequestException, ProcessException, ResponseException;
	
	public void init(String transletName);
	
	public void request() throws RequestException;
	
	public ProcessResult process() throws ProcessException;
	
	public ProcessResult getProcessResult();
	
	public String getForwardTransletName();
	
	public void setForwardTransletName(String forwardingPath);
	
	public void responseEnd();
	
	public void response(Responsible res) throws ResponseException;
	
	public AspectranContext getAspectranContext();
	
	public Responsible getResponse();
	
	public Object getBean(String id);
	
	public ApplicationAdapter getApplicationAdapter();
	
	public String getTransletName();
	
	public RequestScope getRequestScope();
	
	public void setRequestScope(RequestScope requestScope);
	
	public AspectranActivity newAspectranActivity();
	
	public BeanRegistry getBeanRegistry();
	
	public boolean isExceptionRaised();

	public Exception getRaisedException();

	public void setRaisedException(Exception raisedException);
	
	public Object getRequestSetting(String settingName);
	
	public Object getResponseSetting(String settingName);
	
	public Object getTransletSetting(String settingName);
	
	public Object getAspectAdviceBean(String aspectId);
	
}
