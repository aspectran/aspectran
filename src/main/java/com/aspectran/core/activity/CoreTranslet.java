/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity;

import java.util.Map;

import com.aspectran.core.activity.aspect.result.AspectAdviceResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.ResponseNotFoundException;
import com.aspectran.core.activity.response.TransformResponseFactory;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;


/**
 * The Class CoreTranslet.
 */
public class CoreTranslet implements Translet {
	
	protected final Activity activity;
	
	protected Map<String, Object> declaredAttributeMap;
	
	protected ProcessResult processResult;
	
	private AspectAdviceResult aspectAdviceResult;
	
	/**
	 * Instantiates a new core translet.
	 *
	 * @param activity the activity
	 */
	protected CoreTranslet(Activity activity) {
		this.activity = activity;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.translet.Translet#getTransletName()
	 */
	public String getTransletName() {
		return activity.getTransletName();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getDeclaredAttributeMap()
	 */
	public Map<String, Object> getDeclaredAttributeMap() {
		return declaredAttributeMap;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#setDeclaredAttributeMap(java.util.Map)
	 */
	public void setDeclaredAttributeMap(Map<String, Object> declaredAttributeMap) {
		this.declaredAttributeMap = declaredAttributeMap;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getAttribute(java.lang.String)
	 */
	public <T> T getAttribute(String name) {
		if(getRequestAdapter() != null) {
			return getRequestAdapter().getAttribute(name);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getAttribute(java.lang.String, java.lang.Object)
	 */
	public <T> T getAttribute(String name, T defaultValue) {
		T value = null;
		
		if(getRequestAdapter() != null) {
			value = getRequestAdapter().getAttribute(name);
		}
		
		if(value == null)
			return defaultValue;
		
		return value;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		if(getRequestAdapter() != null)
			getRequestAdapter().setAttribute(name, value);
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

	public ProcessResult touchProcessResult() {
		return touchProcessResult(null);
	}
	
	public ProcessResult touchProcessResult(String contentsName) {
		if(processResult == null) {
			processResult = new ProcessResult();
			
			if(contentsName != null)
				processResult.setName(contentsName);
		}

		return processResult;
	}
	
	public void response() {
		activity.activityEnd();
	}

	public void response(Response res) throws ResponseException {
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
		Response res = TransformResponseFactory.getResponse(transformRule);
		
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
		Response res = TransformResponseFactory.getResponse(redirectResponseRule);
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
		Response res = TransformResponseFactory.getResponse(forwardResponseRule);
		response(res);
	}
	
	/**
	 * To respond immediately terminate.
	 */
	public void responseEnd() {
		activity.activityEnd();
	}

	public boolean isExceptionRaised() {
		return activity.isExceptionRaised();
	}

	public Exception getRaisedException() {
		return activity.getRaisedException();
	}

	public ApplicationAdapter getApplicationAdapter() {
		return activity.getActivityContext().getApplicationAdapter();
	}

	public SessionAdapter getSessionAdapter() {
		return activity.getSessionAdapter();
	}
	
	public RequestAdapter getRequestAdapter() {
		return activity.getRequestAdapter();
	}
	
	public ResponseAdapter getResponseAdapter() {
		return activity.getResponseAdapter();
	}
	
	public <T> T getRequestAdaptee() {
		if(getRequestAdapter() != null)
			return getRequestAdapter().getAdaptee();
		else
			return null;
	}
	
	public <T> T getResponseAdaptee() {
		if(getResponseAdapter() != null)
			return getResponseAdapter().getAdaptee();
		else
			return null;
	}

	public <T> T getSessionAdaptee() {
		if(getSessionAdapter() != null)
			return getSessionAdapter().getAdaptee();
		else
			return null;
	}
	
	public <T> T getApplicationAdaptee() {
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
	public <T> T getBean(String id) {
		return activity.getBean(id);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAspectAdviceBean(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return (T)aspectAdviceResult.getAspectAdviceBean(aspectId);
	}
	
	public void putAspectAdviceBean(String aspectId, Object adviceBean) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAspectAdviceBean(aspectId, adviceBean);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBeforeAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return (T)aspectAdviceResult.getBeforeAdviceResult(aspectId);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAfterAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return (T)aspectAdviceResult.getAfterAdviceResult(aspectId);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getFinallyAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return (T)aspectAdviceResult.getFinallyAdviceResult(aspectId);
	}
	
	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
	}

	public Class<? extends Translet> getTransletInterfaceClass() {
		return activity.getTransletInterfaceClass();
	}

	public Class<? extends CoreTranslet> getTransletImplementClass() {
		return activity.getTransletImplementClass();
	}
	
}
