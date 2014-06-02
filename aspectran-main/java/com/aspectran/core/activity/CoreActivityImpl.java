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

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.translet.TransletInstantiationException;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.RequestRule;
import com.aspectran.core.var.rule.ResponseByContentTypeRule;
import com.aspectran.core.var.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.var.rule.ResponseRule;
import com.aspectran.core.var.rule.TransletRule;
import com.aspectran.core.var.type.JoinpointScopeType;
import com.aspectran.core.var.type.ResponseType;

/**
 * <p>Created: 2014. 06. 01 오후 11:00:00</p>
 */
public abstract class CoreActivityImpl extends AbstractCoreActivity implements CoreActivity {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(CoreActivityImpl.class);
	
	/** The debug enabled. */
	private final boolean debugEnabled = logger.isDebugEnabled();

	/** The request adapter. */
	private RequestAdapter requestAdapter;

	/** The response adapter. */
	private ResponseAdapter responseAdapter;

	/** The session adapter. */
	private SessionAdapter sessionAdapter;

	/** The request scope. */
	private Scope requestScope;

	/** The translet rule. */
	private TransletRule transletRule;
	
	/** The request rule. */
	private RequestRule requestRule;
	
	/** The response rule. */
	private ResponseRule responseRule;
	
	/** The translet name. */
	private String transletName;
	
	/** The forward translet name. */
	private String forwardTransletName;

	private boolean withoutResponse;

	public CoreActivityImpl(ActivityContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getRequestAdapter()
	 */
	public RequestAdapter getRequestAdapter() {
		return requestAdapter;
	}
	
	/**
	 * Sets the request adapter.
	 *
	 * @param requestAdapter the new request adapter
	 */
	protected void setRequestAdapter(RequestAdapter requestAdapter) {
		this.requestAdapter = requestAdapter;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getResponseAdapter()
	 */
	public ResponseAdapter getResponseAdapter() {
		return responseAdapter;
	}

	/**
	 * Sets the response adapter.
	 *
	 * @param responseAdapter the new response adapter
	 */
	protected void setResponseAdapter(ResponseAdapter responseAdapter) {
		this.responseAdapter = responseAdapter;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getSessionAdapter()
	 */
	public SessionAdapter getSessionAdapter() {
		return sessionAdapter;
	}
	
	/**
	 * Sets the session adapter.
	 *
	 * @param sessionAdapter the new session adapter
	 */
	protected void setSessionAdapter(SessionAdapter sessionAdapter) {
		this.sessionAdapter = sessionAdapter;
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public void setRequestScope(Scope requestScope) {
		this.requestScope = requestScope;
	}
	
	public void init(String transletName) throws CoreActivityException {
		init(transletName, null);
	}
	
	public void init(String transletName, ProcessResult processResult) throws CoreActivityException {
		if(debugEnabled) {
			logger.debug("run " + transletName);
		}
		
		TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule(transletName);

		if(transletRule == null)
			throw new TransletNotFoundException(transletName);
		
		if(debugEnabled) {
			logger.debug("translet " + transletRule);
		}

		if(transletRule.getTransletInterfaceClass() != null)
			transletInterfaceClass = transletRule.getTransletInterfaceClass();

		if(transletRule.getTransletImplementClass() != null)
			transletImplementClass = transletRule.getTransletImplementClass();

		//create translet instance
		try {
			Constructor<?> transletImplementConstructor = transletImplementClass.getConstructor(CoreActivity.class);
			Object[] args = new Object[] { this };
			
			translet = (CoreTranslet)transletImplementConstructor.newInstance(args);
			
			if(processResult != null)
				translet.setProcessResult(processResult);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletInterfaceClass, transletImplementClass, e);
		}

		this.transletName = transletName;
		this.transletRule = transletRule;
		this.requestRule = transletRule.getRequestRule();
		this.responseRule = transletRule.getResponseRule();
		
		try {
			this.transletAspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry(true);
			this.requestAspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry(true);
			this.responseAspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry(true);

			if(transletRule.getContentList() != null)
				this.contentAspectAdviceRuleRegistry = transletRule.getContentList().getAspectAdviceRuleRegistry(true);
		} catch(CloneNotSupportedException e) {
			throw new CoreActivityException("AspectAdviceRuleRegistry clone failed.", e);
		}
		
		ActivityContext.setCoreActivity(this);
	}
	
	public void run() throws CoreActivityException {
		try {
			withoutResponse = false;
			
			run1st();
		} catch(Exception e) {
			throw new CoreActivityException("aspecran activity run error", e);
		}
	}
	
	public void runWithoutResponse() throws CoreActivityException {
		try {
			withoutResponse = true;
			
			run1st();
		} catch(Exception e) {
			throw new CoreActivityException("aspecran activity run error without response", e);
		}
	}
	
	protected void run1st() throws CoreActivityException {
		try {
			try {
				// execute Before Advice Action for Translet Joinpoint
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = transletAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(isActivityEnd)
					return;
				
				run2nd();
				
				if(isActivityEnd)
					return;
				
				// execute After Advice Action for Translet Joinpoint
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = transletAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					
					if(afterAdviceRuleList != null)
						execute(afterAdviceRuleList);
				}
			} finally {
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = transletAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					
					if(finallyAdviceRuleList != null)
						execute(finallyAdviceRuleList);
				}
			}
		} catch(CoreActivityException e) {
			setRaisedException(e);
			
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = transletRule.getExceptionHandlingRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(isActivityEnd) {
					return;
				}
			}
		
			if(transletAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = transletAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(isActivityEnd) {
						return;
					}
				}
			}
			
			throw e;
		} finally {
			if(requestScope != null) {
				requestScope.destroy();
			}
		}
	}

	private void run2nd() throws CoreActivityException {
		//request
		joinpointScope = JoinpointScopeType.REQUEST;
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = requestAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(isActivityEnd)
					return;
				
				request();
				
				if(isActivityEnd)
					return;
				
				// execute After Advice Action for Request Joinpoint
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = requestAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					
					if(afterAdviceRuleList != null)
						execute(afterAdviceRuleList);
				}
			} finally {
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = requestAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					
					if(finallyAdviceRuleList != null)
						execute(finallyAdviceRuleList);
				}
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(requestAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = requestAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(isActivityEnd) {
						return;
					}
				}
			}
				
			throw new RequestException("request error", e);
		}
		
		if(isActivityEnd)
			return;
		
		//content
		joinpointScope = JoinpointScopeType.CONTENT;
		
		ContentList contentList = transletRule.getContentList();
		
		if(contentList != null) {
			try {
				try {
					// execute Before Advice Action for Content Joinpoint
					if(contentAspectAdviceRuleRegistry != null) {
						List<AspectAdviceRule> beforeAdviceRuleList = contentAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

						if(beforeAdviceRuleList != null)
							execute(beforeAdviceRuleList);
					}
					
					if(isActivityEnd)
						return;
					
					process();
					
					if(isActivityEnd)
						return;
					
					// execute After Advice Action for Content Joinpoint
					if(contentAspectAdviceRuleRegistry != null) {
						List<AspectAdviceRule> afterAdviceRuleList = contentAspectAdviceRuleRegistry.getAfterAdviceRuleList();
						
						if(afterAdviceRuleList != null)
							execute(afterAdviceRuleList);
					}
				} finally {
					if(contentAspectAdviceRuleRegistry != null) {
						List<AspectAdviceRule> finallyAdviceRuleList = contentAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
						
						if(finallyAdviceRuleList != null)
							execute(finallyAdviceRuleList);
					}
				}
			} catch(Exception e) {
				setRaisedException(e);
				
				if(contentAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> exceptionRaizedAdviceRuleList = contentAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
					
					if(exceptionRaizedAdviceRuleList != null) {
						responseByContentType(exceptionRaizedAdviceRuleList);

						if(isActivityEnd) {
							return;
						}
					}
				}
				
				throw new ProcessException("process error", e);
			}
		}
		
		/*
		if(logger.isDebugEnabled()) {
			if(getProcessResult() != null) {
				logger.debug("contentResult:");
				for(ContentResult contentResult : getProcessResult()) {
					for(ActionResult actionResult : contentResult) {
						logger.debug("\t{actionId: " + actionResult.getActionId() + ", resultValue: " + actionResult.getResultValue() + "}\n");
					}
				}
			}
		}
		*/
		
		if(isActivityEnd || withoutResponse)
			return;
		
		//response
		joinpointScope = JoinpointScopeType.RESPONSE;
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = responseAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(isActivityEnd)
					return;
				
				response();
				
				if(isActivityEnd)
					return;
				
				// execute After Advice Action for Request Joinpoint
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = responseAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					
					if(afterAdviceRuleList != null)
						execute(afterAdviceRuleList);
				}
			} finally {
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = responseAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					
					if(finallyAdviceRuleList != null)
						execute(finallyAdviceRuleList);
				}
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(responseAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = responseAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(isActivityEnd) {
						return;
					}
				}
			}
			
			throw new ResponseException("response error", e);
		}
	}
	
	public void request() throws RequestException {
		request(translet);
	}
	
	protected abstract void request(CoreTranslet translet) throws RequestException;
	
	public ProcessResult process() throws CoreActivityException {
		// execute action on contents area
		ContentList contentList = transletRule.getContentList();
		
		if(contentList != null) {
			for(ActionList actionList : contentList) {
				execute(actionList);
				
				if(isActivityEnd)
					break;
			}
		}
		
		return translet.getProcessResult();
	}
	
	/**
	 * The <code>response</code> will return to find.
	 *
	 * @return the response
	 */
	public Responsible getResponse() {
		if(responseRule == null)
			return null;

		return responseRule.getResponse();
	}
	
	protected void response() throws CoreActivityException {
		Responsible res = getResponse();
		
		if(res != null)
			response(res);
		
		if(forwardTransletName != null)
			forward();
	}
	
	/**
	 * Response.
	 * 
	 * @param res the responsible
	 * 
	 * @throws ResponseException the response exception
	 */
	public void response(Responsible res) throws ResponseException {
		res.response(this);
		
		if(res.getResponseType() == ResponseType.FORWARD) {
			ForwardResponse forwardResponse = (ForwardResponse)res;
			String forwardTransletName = forwardResponse.getForwardResponseRule().getTransletName();
			setForwardTransletName(forwardTransletName);
		}
		
		activityEnd();
	}
	
	/**
	 * Forwarding.
	 *
	 * @throws ResponseException the active response exception
	 */
	private void forward() throws CoreActivityException {
		if(debugEnabled) {
			logger.debug("forwarding for translet '" + forwardTransletName + "'");
		}
		
		ProcessResult processResult = translet.getProcessResult();
		init(forwardTransletName, processResult);
		run();
	}
	
	public void responseByContentType(List<AspectAdviceRule> aspectAdviceRuleList) throws CoreActivityException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = aspectAdviceRule.getResponseByContentTypeRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(isActivityEnd)
					return;
			}
		}
	}

	private void responseByContentType(ResponseByContentTypeRuleMap responseByContentTypeRuleMap) throws CoreActivityException {
		ResponseByContentTypeRule rbctr = responseByContentTypeRuleMap.getResponseByContentTypeRule(getRaisedException());
		responseByContentType(rbctr);
	}

	/**
	 * Response by content type.
	 *
	 * @param responseByContentTypeRule the response by content type rule
	 * @throws ResponseException 
	 * @throws ProcessException 
	 * @throws RequestException 
	 */
	private void responseByContentType(ResponseByContentTypeRule responseByContentTypeRule) throws CoreActivityException {
		Responsible response = getResponse();
		
		if(response != null && response.getContentType() != null) {
			Responsible response2 = responseByContentTypeRule.getResponse(response.getContentType());
			ResponseRule newResponseRule = responseRule.newResponseRule(response2);
			responseRule = newResponseRule;
			
			if(debugEnabled) {
				logger.debug("response by content-type: " + responseRule);
			}

			translet.setProcessResult(null);
			
			if(responseRule.getResponse() != null) {
				ActionList actionList = responseRule.getResponse().getActionList();
				
				if(actionList != null)
					execute(actionList);

				response();
			}
		}
	}
	
	public String getForwardTransletName() {
		return forwardTransletName;
	}

	protected void setForwardTransletName(String forwardTransletName) {
		this.forwardTransletName = forwardTransletName;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getTransletName()
	 */
	public String getTransletName() {
		return transletName;
	}
	
	/**
	 * Gets the translet rule.
	 *
	 * @return the translet rule
	 */
	protected TransletRule getTransletRule() {
		return transletRule;
	}

	/**
	 * Gets the request rule.
	 *
	 * @return the request rule
	 */
	protected RequestRule getRequestRule() {
		return requestRule;
	}

	/**
	 * Gets the response rule.
	 *
	 * @return the response rule
	 */
	protected ResponseRule getResponseRule() {
		return responseRule;
	}

	public abstract CoreActivity newCoreActivity();
	
}
