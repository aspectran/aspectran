/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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

import java.util.Map;

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;

/**
 * <p>Created: 2008. 7. 5. 오전 12:35:44</p>
 */
public abstract interface Translet {

	/**
	 * Returns the path of translet.
	 * 
	 * @return the translet path
	 */
	public String getTransletName();

	/**
	 * Gets the declared attribute map.
	 *
	 * @return the declared attribute map
	 */
	public Map<String, Object> getDeclaredAttributeMap();
	
	/**
	 * Sets the declared attribute map.
	 *
	 * @param declaredAttributeMap the declared attribute map
	 */
	public void setDeclaredAttributeMap(Map<String, Object> declaredAttributeMap);
	
	/**
	 * Gets the process result.
	 * 
	 * @return the process result
	 */
	public ProcessResult getProcessResult();

	/**
	 * Sets the process result.
	 * 
	 * @param processResult the new process result
	 */
	public void setProcessResult(ProcessResult processResult);

	public ProcessResult touchProcessResult();
	
	public ProcessResult touchProcessResult(String contentName);
	
	/**
	 * Response.
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response() throws ResponseException;

	/**
	 * Response.
	 * 
	 * @param res the responsible
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response(Response res) throws ResponseException;

	public void transform(TransformRule transformRule) throws ResponseException;

	public void redirect(RedirectResponseRule redirectResponseRule) throws ResponseException;
	
	public void forward(ForwardResponseRule forwardResponseRule) throws ResponseException;
	
	public ApplicationAdapter getApplicationAdapter();
	
	public SessionAdapter getSessionAdapter();
	
	public RequestAdapter getRequestAdapter();
	
	public ResponseAdapter getResponseAdapter();
	
	public <T> T getRequestAdaptee();
	
	public <T> T getResponseAdaptee();
	
	public <T> T getSessionAdaptee();
	
	public <T> T getBean(String beanId);
	
	/**
	 * To respond immediately terminate.
	 */
	public void responseEnd();
	
	public Class<? extends Translet> getTransletInterfaceClass();

	public Class<? extends CoreTranslet> getTransletImplementClass();
	
	public boolean isExceptionRaised();

	public Exception getRaisedException();
	
	public <T> T getAspectAdviceBean(String aspectId);
	
	public void putAspectAdviceBean(String aspectId, Object adviceBean);
	
	public <T> T getBeforeAdviceResult(String aspectId);
	
	public <T> T getAfterAdviceResult(String aspectId);
	
	public <T> T getFinallyAdviceResult(String aspectId);

	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult);
}
