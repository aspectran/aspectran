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

import java.util.Map;

import com.aspectran.core.activity.aspect.result.AspectAdviceResult;
import com.aspectran.core.activity.process.result.ActionResult;
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
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.ForwardResponseRule;
import com.aspectran.core.var.rule.RedirectResponseRule;
import com.aspectran.core.var.rule.TransformRule;

/**
 * The Class ActiveTranslet.
 */
public abstract class AbstractCoreTranslet implements CoreTranslet {
	
	protected final CoreActivity activity;
	
	protected Map<String, Object> declaredAttributeMap;
	
	protected ProcessResult processResult;
	
	private ContentResult contentResult;
	
	private AspectAdviceResult aspectAdviceResult;
	
	/**
	 * Instantiates a new active translet.
	 * 
	 * @param transletRule the translet rule
	 * @param output the output
	 */
	protected AbstractCoreTranslet(CoreActivity activity) {
		this.activity = activity;
	}
	
	public Map<String, Object> getDeclaredAttributeMap() {
		return declaredAttributeMap;
	}

	public void setDeclaredAttributeMap(Map<String, Object> declaredAttributeMap) {
		this.declaredAttributeMap = declaredAttributeMap;
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
		
		this.contentResult = contentResult;
	}
	
	public void addActionResult(ActionResult actionResult) {
		if(contentResult != null) {
			contentResult.add(actionResult);
		}
	}
	
	public void addActionResult(String actionId, Object resultValue) {
		if(contentResult != null) {
			contentResult.addActionResult(actionId, resultValue);
		}
	}

	public void response() {
		activity.responseEnd();
	}

	public void response(Responsible res) throws ResponseException {
		activity.response(res);
	}
	
	/**
	 * Transform.
	 * 
	 * @param actionId the id
	 * 
	 * @throws ResponseException the response exception
	 */
	public void transform(TransformRule transformRule) throws ResponseException {
		Responsible res = TransformResponseFactory.getResponse(transformRule);
		
		if(res == null)
			throw new ResponseNotFoundException("transform response is not found. transformRule" + transformRule);

		response(res);
	}

	/**
	 * Redirect.
	 * 
	 * @param actionId the id
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
	 * @param actionId the id
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
		return activity.getActivityContext().getApplicationAdapter();
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
		ApplicationAdapter applicationAdapter = activity.getActivityContext().getApplicationAdapter();
		
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

	public Class<? extends CoreTranslet> getTransletInterfaceClass() {
		return activity.getTransletInterfaceClass();
	}

	public Class<? extends AbstractCoreTranslet> getTransletImplementClass() {
		return activity.getTransletImplementClass();
	}

	public boolean isExceptionRaised() {
		return activity.isExceptionRaised();
	}

	public Exception getRaisedException() {
		return activity.getRaisedException();
	}
	
	public Object getAspectAdviceBean(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return aspectAdviceResult.getAspectAdviceBean(aspectId);
	}
	
	public void putAspectAdviceBean(String aspectId, Object adviceBean) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAspectAdviceBean(aspectId, adviceBean);
	}
	
	public Object getBeforeAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return aspectAdviceResult.getBeforeAdviceResult(aspectId);
	}
	
	public Object getAfterAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return aspectAdviceResult.getAfterAdviceResult(aspectId);
	}
	
	public Object getFinallyAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return aspectAdviceResult.getFinallyAdviceResult(aspectId);
	}
	
	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
	}
	
}
