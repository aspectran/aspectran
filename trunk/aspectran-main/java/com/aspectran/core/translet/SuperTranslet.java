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
package com.aspectran.core.translet;

import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.base.adapter.RequestAdapter;
import com.aspectran.base.adapter.ResponseAdapter;
import com.aspectran.base.adapter.SessionAdapter;
import com.aspectran.base.context.AspectranContext;
import com.aspectran.base.rule.ForwardResponseRule;
import com.aspectran.base.rule.RedirectResponseRule;
import com.aspectran.base.rule.TransformRule;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;

/**
 * <p>Created: 2008. 7. 5. 오전 12:35:44</p>
 */
public abstract interface SuperTranslet {

	/**
	 * Returns the path of translet.
	 * 
	 * @return the translet path
	 */
	public String getTransletName();

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
	 * Response.
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response() throws ResponseException;

	/**
	 * Response.
	 * 
	 * @param id the id
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response(String id) throws ResponseException;

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
	
	public AspectranContext getContext();
	
	/**
	 * Checks if is max length exceeded.
	 * 
	 * @return true, if checks if is max length exceeded
	 */
	public boolean isMaxLengthExceeded();
	
	/**
	 * To respond immediately terminate.
	 */
	public void responseEnd();
}
