/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.TransformResponseFactory;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Class CoreTranslet.
 */
public class CoreTranslet implements Translet {
	
	protected final Activity activity;
	
	protected Map<String, Object> declaredAttributeMap;
	
	protected ProcessResult processResult;
	
	private AspectAdviceResult aspectAdviceResult;

	private ActivityResultDataMap activityResultDataMap;
	
	/**
	 * Instantiates a new CoreTranslet.
	 *
	 * @param activity the current Activity
	 */
	protected CoreTranslet(Activity activity) {
		this.activity = activity;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getTransletName()
	 */
	public String getTransletName() {
		return activity.getTransletName();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getRestVerb()
	 */
	public RequestMethodType getRestVerb() {
		return activity.getRestVerb();
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
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getProcessResult()
	 */
	public ProcessResult getProcessResult() {
		return processResult;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getProcessResult(java.lang.String)
	 */
	public Object getProcessResult(String actionId) {
		if(processResult == null)
			return null;

		return processResult.getResultValue(actionId);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#setProcessResult(com.aspectran.core.activity.process.result.ProcessResult)
	 */
	public void setProcessResult(ProcessResult processResult) {
		this.processResult = processResult;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#touchProcessResult()
	 */
	public ProcessResult touchProcessResult() {
		return touchProcessResult(null);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#touchProcessResult(java.lang.String)
	 */
	public ProcessResult touchProcessResult(String contentsName) {
		if(processResult == null) {
			processResult = new ProcessResult();
			
			if(contentsName != null)
				processResult.setName(contentsName);
		}

		return processResult;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getActivityResultDataMap()
	 */
	public ActivityResultDataMap getActivityResultDataMap() {
		if(activityResultDataMap == null) {
			activityResultDataMap = new ActivityResultDataMap(activity);
		}

		return activityResultDataMap;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#response()
	 */
	public void response() {
		activity.activityEnd();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#response(com.aspectran.core.activity.response.Response)
	 */
	public void response(Response response) {
		activity.response(response);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#transform(com.aspectran.core.context.rule.TransformRule)
	 */
	public void transform(TransformRule transformRule) {
		Response res = TransformResponseFactory.getResponse(transformRule);
		response(res);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#redirect(com.aspectran.core.context.rule.RedirectResponseRule)
	 */
	public void redirect(RedirectResponseRule redirectResponseRule) {
		Response res = new RedirectResponse(redirectResponseRule);
		response(res);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#forward(com.aspectran.core.context.rule.ForwardResponseRule)
	 */
	public void forward(ForwardResponseRule forwardResponseRule) {
		Response res = new ForwardResponse(forwardResponseRule);
		response(res);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#isExceptionRaised()
	 */
	public boolean isExceptionRaised() {
		return activity.isExceptionRaised();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getRaisedException()
	 */
	public Exception getRaisedException() {
		return activity.getRaisedException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getApplicationAdapter()
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return activity.getActivityContext().getApplicationAdapter();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getSessionAdapter()
	 */
	public SessionAdapter getSessionAdapter() {
		return activity.getSessionAdapter();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getRequestAdapter()
	 */
	public RequestAdapter getRequestAdapter() {
		return activity.getRequestAdapter();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getResponseAdapter()
	 */
	public ResponseAdapter getResponseAdapter() {
		return activity.getResponseAdapter();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getRequestAdaptee()
	 */
	public <T> T getRequestAdaptee() {
		if(getRequestAdapter() != null)
			return getRequestAdapter().getAdaptee();
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getResponseAdaptee()
	 */
	public <T> T getResponseAdaptee() {
		if(getResponseAdapter() != null)
			return getResponseAdapter().getAdaptee();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getSessionAdaptee()
	 */
	public <T> T getSessionAdaptee() {
		if(getSessionAdapter() != null)
			return getSessionAdapter().getAdaptee();
		else
			return null;
	}
	
	/**
	 * Gets the application adaptee.
	 *
	 * @param <T> the generic type
	 * @return the application adaptee
	 */
	public <T> T getApplicationAdaptee() {
		ApplicationAdapter applicationAdapter = activity.getActivityContext().getApplicationAdapter();
		
		if(applicationAdapter != null)
			return applicationAdapter.getAdaptee();
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getBean(java.lang.String)
	 */
	public <T> T getBean(String id) {
		return activity.getBean(id);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getBean(java.lang.Class)
	 */
	public <T> T getBean(Class<T> classType) {
		return activity.getBean(classType);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getBean(java.lang.String, java.lang.Class)
	 */
	public <T> T getBean(String id, Class<T> classType) {
		return activity.getBean(id, classType);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getAspectAdviceBean(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAspectAdviceBean(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return (T)aspectAdviceResult.getAspectAdviceBean(aspectId);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#putAspectAdviceBean(java.lang.String, java.lang.Object)
	 */
	public void putAspectAdviceBean(String aspectId, Object adviceBean) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAspectAdviceBean(aspectId, adviceBean);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getBeforeAdviceResult(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBeforeAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;
		
		return (T)aspectAdviceResult.getBeforeAdviceResult(aspectId);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getAfterAdviceResult(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAfterAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return (T)aspectAdviceResult.getAfterAdviceResult(aspectId);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getFinallyAdviceResult(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getFinallyAdviceResult(String aspectId) {
		if(aspectAdviceResult == null)
			return null;

		return (T)aspectAdviceResult.getFinallyAdviceResult(aspectId);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#putAdviceResult(com.aspectran.core.context.rule.AspectAdviceRule, java.lang.Object)
	 */
	public void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
		if(aspectAdviceResult == null)
			aspectAdviceResult = new AspectAdviceResult();
		
		aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getTransletInterfaceClass()
	 */
	public Class<? extends Translet> getTransletInterfaceClass() {
		return activity.getTransletInterfaceClass();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.Translet#getTransletImplementClass()
	 */
	public Class<? extends CoreTranslet> getTransletImplementClass() {
		return activity.getTransletImplementClass();
	}
	
}
