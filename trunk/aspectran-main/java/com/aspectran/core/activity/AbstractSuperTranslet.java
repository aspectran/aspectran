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

import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.ResponseNotFoundException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.activity.response.TransformResponseFactory;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.rule.ForwardResponseRule;
import com.aspectran.core.rule.RedirectResponseRule;
import com.aspectran.core.rule.TransformRule;

/**
 * The Class ActiveTranslet.
 */
public abstract class AbstractSuperTranslet implements SuperTranslet {
	
	protected final AspectranActivity activity;
	
	protected ProcessResult processResult;
	
	/**
	 * Instantiates a new active translet.
	 * 
	 * @param transletRule the translet rule
	 * @param output the output
	 */
	protected AbstractSuperTranslet(AspectranActivity activity) {
		this.activity = activity;
	}
	
	/**
	 * Gets the bean registry.
	 *
	 * @return the bean registry
	 */
	public BeanRegistry getBeanFactory() {
		return activity.getContext().getBeanRegistry();
	}

	/**
	 * Gets the process result.
	 * 
	 * @return the process result
	 */
	public ProcessResult getProcessResult() {
		return processResult;
	}

	/**
	 * Sets the process result.
	 * 
	 * @param processResult the new process result
	 */
	public void setProcessResult(ProcessResult processResult) {
		this.processResult = processResult;
	}

	/**
	 * Adds the content result.
	 * 
	 * @param contentResult the content result
	 */
	public void addContentResult(ContentResult contentResult) {
		if(processResult == null)
			processResult = new ProcessResult();
		
		processResult.add(contentResult);
	}

	public void response() {
		activity.responseEnd();
	}

	/**
	 * Response.
	 *
	 * @param responseId the response id
	 * @throws ResponseException the response exception
	 */
	public void response(String responseId) throws ResponseException {
		Responsible res = getResponse(responseId);
		
		if(res == null)
			throw new ResponseNotFoundException("'" + responseId + "' response is not found.");
		
		response(res);
	}
	
	public void response(Responsible res) throws ResponseException {
		activity.response(res);
	}
	
	/**
	 * Transform.
	 * 
	 * @param id the id
	 * 
	 * @throws ResponseException the response exception
	 */
	public void transform(TransformRule transformRule) throws ResponseException {
		Responsible res = TransformResponseFactory.getResponse(transformRule);
		
		if(res == null)
			throw new ResponseNotFoundException("transform response is not found. TransformRule" + transformRule);

		response(res);
	}

	/**
	 * Redirect.
	 * 
	 * @param id the id
	 * 
	 * @throws ResponseException the response exception
	 */
	public void redirect(RedirectResponseRule redirectResponseRule) throws ResponseException {
		Responsible res = TransformResponseFactory.getResponse(redirectResponseRule);
		response(res);
	}
	
	/**
	 * Forward.
	 * 
	 * @param id the id
	 * 
	 * @throws ResponseException the response exception
	 */
	public void forward(ForwardResponseRule forwardResponseRule) throws ResponseException {
		Responsible res = TransformResponseFactory.getResponse(forwardResponseRule);
		response(res);
	}
	
	public RequestAdapter getRequestAdapter() {
		return activity.getRequestAdapter();
	}
	
	public ResponseAdapter getResponseAdapter() {
		return activity.getResponseAdapter();
	}

	public SessionAdapter getSessionAdapter() {
		return activity.getSessionAdapter();
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return activity.getContext().getApplicationAdapter();
	}
	
	public Object getRequestAdaptee() {
		if(getRequestAdapter() != null)
			return getRequestAdapter().getAdaptee();
		else
			return null;
	}
	
	public Object getResponseAdaptee() {
		if(getResponseAdapter() != null)
			return getResponseAdapter().getAdaptee();
		else
			return null;
	}

	public Object getSessionAdaptee() {
		if(getSessionAdapter() != null)
			return getSessionAdapter().getAdaptee();
		else
			return null;
	}
	
	public Object getApplicationAdaptee() {
		ApplicationAdapter applicationAdapter = activity.getContext().getApplicationAdapter();
		
		if(applicationAdapter != null)
			return applicationAdapter.getAdaptee();
		else
			return null;
	}
	
	/**
	 * Gets the bean.
	 *
	 * @param id the id
	 * @return the bean
	 */
	public Object getBean(String id) {
		return activity.getBean(id);
	}

	public AspectranContext getAspectranContext() {
		return activity.getContext();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.translet.Translet#getTransletName()
	 */
	public String getTransletName() {
		return activity.getTransletName();
	}
	
	/**
	 * To respond immediately terminate.
	 */
	public void responseEnd() {
		activity.responseEnd();
	}

	public Responsible getResponse(String responseId) {
		return activity.getResponse(responseId);
	}

	public Class<? extends SuperTranslet> getTransletInterfaceClass() {
		return activity.getTransletInstanceClass();
	}

	public Class<? extends AbstractSuperTranslet> getTransletInstanceClass() {
		return activity.getTransletInstanceClass();
	}

	public boolean isExceptionRaised() {
		return activity.isExceptionRaised();
	}

	public Exception getRaisedException() {
		return activity.getRaisedException();
	}
	
}
