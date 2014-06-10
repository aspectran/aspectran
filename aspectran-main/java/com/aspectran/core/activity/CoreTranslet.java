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

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.ForwardResponseRule;
import com.aspectran.core.var.rule.RedirectResponseRule;
import com.aspectran.core.var.rule.TransformRule;

/**
 * <p>Created: 2008. 7. 5. 오전 12:35:44</p>
 */
public abstract interface CoreTranslet {

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

	/**
	 * Adds the content result.
	 * 
	 * @param contentResult the content result
	 */
	public void addContentResult(ContentResult contentResult);

	/**
	 * Adds the action result.
	 *
	 * @param actionResult the action result
	 */
	public void addActionResult(ActionResult actionResult);
	
	/**
	 * Adds the action result.
	 *
	 * @param actionId the action id
	 * @param resultValue the result value
	 */
	public void addActionResult(String actionId, Object resultValue);

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
	public void response(Responsible res) throws ResponseException;

	public void transform(TransformRule transformRule) throws ResponseException;

	public void redirect(RedirectResponseRule redirectResponseRule) throws ResponseException;
	
	public void forward(ForwardResponseRule forwardResponseRule) throws ResponseException;
	
	public RequestAdapter getRequestAdapter();
	
	public ResponseAdapter getResponseAdapter();
	
	public SessionAdapter getSessionAdapter();
	
	public ApplicationAdapter getApplicationAdapter();
	
	public Object getRequestAdaptee();
	
	public Object getResponseAdaptee();
	
	public Object getSessionAdaptee();
	
	public Object getBean(String beanId);
	
	/**
	 * To respond immediately terminate.
	 */
	public void responseEnd();
	
	public Class<? extends CoreTranslet> getTransletInterfaceClass();

	public Class<? extends CoreTransletImpl> getTransletImplementClass();
	
	public boolean isExceptionRaised();

	public Exception getRaisedException();
	
	public Object getAspectAdviceBean(String aspectId);
	
	public void putAspectAdviceBean(String aspectId, Object adviceBean);
	
	public Object getBeforeAdviceResult(String aspectId);
	
	public Object getAfterAdviceResult(String aspectId);
	
	public Object getFinallyAdviceResult(String aspectId);

	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult);
}
