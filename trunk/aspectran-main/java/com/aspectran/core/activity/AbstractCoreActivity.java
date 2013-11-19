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

import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.translet.TransletInstantiationException;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.rule.ResponseByContentTypeRule;
import com.aspectran.core.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.rule.ResponseRule;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.type.ActionType;
import com.aspectran.core.type.ResponseType;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public abstract class AbstractCoreActivity implements CoreActivity {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(AbstractCoreActivity.class);
	
	/** The debug enabled. */
	private final boolean debugEnabled = logger.isDebugEnabled();

	/** The context. */
	protected final AspectranContext context;
	
	/** The request adapter. */
	private RequestAdapter requestAdapter;

	/** The response adapter. */
	private ResponseAdapter responseAdapter;

	/** The session adapter. */
	private SessionAdapter sessionAdapter;

	/** The translet interface class. */
	private Class<? extends CoreTranslet> transletInterfaceClass;
	
	/** The translet instance class. */
	private Class<? extends AbstractCoreTranslet> transletImplementClass;

	/** The translet rule. */
	private TransletRule transletRule;
	
	/** The request rule. */
	private RequestRule requestRule;
	
	/** The response rule. */
	private ResponseRule responseRule;
	
	/** The translet. */
	private CoreTranslet translet;
	
	/** The request scope. */
	private RequestScope requestScope;
	
	/** Whether the response is ended. */
	private boolean isResponseEnd;

	/** The forward translet name. */
	private String forwardTransletName;
	
	private Exception raisedException;

	/** The translet name. */
	private String transletName;
	
	//private AspectAdviceResult aspectAdviceResult;
	
	/**
	 * Instantiates a new action translator.
	 *
	 * @param context the translets context
	 */
	public AbstractCoreActivity(AspectranContext context) {
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getRequestAdapter()
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
	 * @see com.aspectran.core.activity.AspectranActivity#getResponseAdapter()
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
	 * @see com.aspectran.core.activity.AspectranActivity#getSessionAdapter()
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
	
	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends CoreTranslet> getTransletInterfaceClass() {
		if(transletRule != null && transletRule.getTransletInterfaceClass() != null)
			return transletRule.getTransletInterfaceClass();
		
		return transletInterfaceClass;
	}

	/**
	 * Sets the translet interface class.
	 *
	 * @param transletInterfaceClass the new translet interface class
	 */
	public void setTransletInterfaceClass(Class<? extends CoreTranslet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	/**
	 * Gets the translet instance class.
	 *
	 * @return the translet instance class
	 */
	public Class<? extends AbstractCoreTranslet> getTransletImplementClass() {
		if(transletRule != null && transletRule.getTransletImplementClass() != null)
			return transletRule.getTransletImplementClass();

		return transletImplementClass;
	}

	/**
	 * Sets the translet instance class.
	 *
	 * @param transletInstanceClass the new translet instance class
	 */
	public void setTransletImplementClass(Class<? extends AbstractCoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}

	public CoreTranslet getSuperTranslet() {
		return translet;
	}

	public void init(String transletName) {
		if(debugEnabled) {
			logger.debug(">> " + transletName);
		}
		
		TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule(transletName);

		if(transletRule == null)
			throw new TransletNotFoundException(transletName);
		
		if(debugEnabled) {
			logger.debug("translet " + transletRule);
		}

		Class<? extends CoreTranslet> transletInterfaceClass = getTransletInterfaceClass();
		Class<? extends AbstractCoreTranslet> transletImplementClass = getTransletImplementClass();

		//create translet instance
		try {
			Constructor<?> transletImplementConstructor = transletImplementClass.getConstructor(CoreActivity.class, boolean.class);
			Object[] args = new Object[] { this, false };
			
			if(transletRule.isAspectAdviceRuleExists())
				args[1] = true;
			
			translet = (CoreTranslet)transletImplementConstructor.newInstance(args);
		} catch(Exception e) {
			throw new TransletInstantiationException(transletInterfaceClass, transletImplementClass, e);
		}

		this.transletName = transletName;
		this.transletRule = transletRule;
		this.requestRule = transletRule.getRequestRule();
		this.responseRule = transletRule.getResponseRule();
	}
	
	public void run() throws CoreActivityException {
		try {
			run(null);
		} catch(Exception e) {
			throw new CoreActivityException("aspecran activity error", e);
		}
	}
	
	protected void run(ProcessResult processResult) throws CoreActivityException {
		if(processResult != null) {
			translet.setProcessResult(processResult);
		}

		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry();
		
		try {
			if(aspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	
				if(finallyAdviceRuleList != null) {
					try {
						run2(aspectAdviceRuleRegistry);
					} finally {
						execute(finallyAdviceRuleList);
					}
				} else {
					run2(aspectAdviceRuleRegistry);
				}
			} else {
				run2();
			}
		} catch(CoreActivityException e) {
			setRaisedException(e);
			
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = transletRule.getExceptionHandlingRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(isResponseEnd) {
					return;
				}
			}
		
			List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
			
			if(exceptionRaizedAdviceRuleList != null) {
				responseByContentType(exceptionRaizedAdviceRuleList);
				
				if(isResponseEnd) {
					return;
				}
			}
			
			throw e;
		} finally {
			if(requestScope != null) {
				requestScope.destroy();
			}
		}
	}

	private void run2(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws CoreActivityException {
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		// execute Before Advice Action
		if(beforeAdviceRuleList != null)
			execute(beforeAdviceRuleList);
		
		if(isResponseEnd)
			return;
		
		run2();
		
		if(isResponseEnd)
			return;
		
		// execute After Advice Action
		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
	}

	private void run2() throws CoreActivityException {
		//request
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry();
		List<AspectAdviceRule> finallyAdviceRuleList = null;

		try {
			if(aspectAdviceRuleRegistry != null) {
				finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	
				if(finallyAdviceRuleList != null) {
					try {
						request(aspectAdviceRuleRegistry);
					} finally {
						execute(finallyAdviceRuleList);
					}
				} else {
					request(aspectAdviceRuleRegistry);
				}
			} else {
				request();
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(aspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(isResponseEnd) {
						return;
					}
				}
			}
				
			throw new RequestException("request error", e);
		}
		
		if(isResponseEnd)
			return;
		
		//content
		ContentList contentList = transletRule.getContentList();
		
		if(contentList != null) {
			try {
				aspectAdviceRuleRegistry = contentList.getAspectAdviceRuleRegistry();
		
				if(aspectAdviceRuleRegistry != null) {
					finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
		
					if(finallyAdviceRuleList != null) {
						try {
							process(aspectAdviceRuleRegistry);
						} finally {
							execute(finallyAdviceRuleList);
						}
					} else {
						process(aspectAdviceRuleRegistry);
					}
				} else {
					process();
				}
			} catch(Exception e) {
				setRaisedException(e);
				
				if(aspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
					
					if(exceptionRaizedAdviceRuleList != null) {
						responseByContentType(exceptionRaizedAdviceRuleList);

						if(isResponseEnd) {
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
		
		if(isResponseEnd)
			return;
		
		//response
		aspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry();

		try {
			if(aspectAdviceRuleRegistry != null) {
				finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	
				if(finallyAdviceRuleList != null) {
					try {
						response(aspectAdviceRuleRegistry);
					} finally {
						execute(finallyAdviceRuleList);
					}
				} else {
					response(aspectAdviceRuleRegistry);
				}
			} else {
				response();
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(aspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(isResponseEnd) {
						return;
					}
				}
			}
			
			throw new ResponseException("response error", e);
		}
	}
	
	private void request(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws RequestException, ActionExecutionException {
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		// execute Before Advice Action
		if(beforeAdviceRuleList != null)
			execute(beforeAdviceRuleList);
		
		if(isResponseEnd)
			return;
		
		request();
		
		if(isResponseEnd)
			return;
		
		// execute After Advice Action
		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
	}
	
	public void request() throws RequestException {
		request(translet);
	}
	
	protected abstract void request(CoreTranslet translet) throws RequestException;
	
	public ProcessResult process(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws CoreActivityException {
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		// execute Before Advice Action
		if(beforeAdviceRuleList != null)
			execute(beforeAdviceRuleList);
		
		if(isResponseEnd)
			return translet.getProcessResult();
		
		process();
		
		if(isResponseEnd)
			return translet.getProcessResult();
		
		// execute After Advice Action
		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
		
		return translet.getProcessResult();
	}
	
	public ProcessResult process() throws CoreActivityException {
		// execute action on contents area
		ContentList contentList = transletRule.getContentList();
		
		if(contentList != null) {
			for(ActionList actionList : contentList) {
				execute(actionList);
				
				if(isResponseEnd)
					break;
			}
		}
		
		return translet.getProcessResult();
	}
	
	public ProcessResult getProcessResult() {
		if(translet == null)
			return null;
		
		return translet.getProcessResult();
	}
	
	private void response(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws CoreActivityException {
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		// execute Before Advice Action
		if(beforeAdviceRuleList != null)
			execute(beforeAdviceRuleList);
		
		if(isResponseEnd)
			return;
		
		response();
		
		if(isResponseEnd)
			return;
		
		// execute After Advice Action
		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
	}
	
	protected void response() throws CoreActivityException {
		Responsible res = getResponse();
		
		if(res != null)
			response(res);
		
		if(forwardTransletName != null)
			forward();
	}
	
	/**
	 * Execute.
	 *
	 * @param actionList the action list
	 * @throws CoreActivityException 
	 */
	private void execute(ActionList actionList) throws CoreActivityException {
		if(debugEnabled) {
			logger.debug("executable actions " + actionList.toString());
		}
		
		if(!actionList.isHidden()) {
			ContentResult contentResult = new ContentResult();
			contentResult.setContentId(actionList.getContentId());

			translet.addContentResult(contentResult);
		}
		
		for(Executable action : actionList) {
			AspectAdviceRuleRegistry aspectAdviceRuleRegistry = action.getAspectAdviceRuleRegistry();
			List<AspectAdviceRule> finallyAdviceRuleList = null;
			
			try {
				if(aspectAdviceRuleRegistry != null) {
					finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					
					if(finallyAdviceRuleList != null) {
						try {
							execute(action, aspectAdviceRuleRegistry);
						} finally {
							execute(finallyAdviceRuleList);
						}
					} else {
						execute(action, aspectAdviceRuleRegistry);
					}
				} else {
					execute(action);
				}
			} catch(Exception e) {
				setRaisedException(e);
				
				if(aspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> exceptionRaizedAdviceRuleList = aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
					
					if(exceptionRaizedAdviceRuleList != null) {
						responseByContentType(exceptionRaizedAdviceRuleList);
						
						if(isResponseEnd) {
							return;
						}
					}
				}
				
				throw new ActionExecutionException("action execution error", e);
			}
			
			if(isResponseEnd)
				break;
		}
	}
	
	private void execute(Executable action, AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws ActionExecutionException {
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		// execute Before Advice Action
		if(beforeAdviceRuleList != null)
			execute(beforeAdviceRuleList);
		
		if(isResponseEnd)
			return;
		
		execute(action);
		
		if(isResponseEnd)
			return;
		
		// execute After Advice Action
		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
	}
	
	private void execute(Executable action) throws ActionExecutionException {
		if(debugEnabled)
			logger.debug("execute action " + action.toString());
		
		try {
			Object resultValue = action.execute(this);
		
			if(debugEnabled)
				logger.debug("action " + action + " result: " + resultValue);
			
			if(!action.isHidden() && resultValue != ActionResult.NO_RESULT) {
				translet.addActionResult(action.getActionId(), resultValue);
			}
		} catch(Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("action execution error", e);
		}
	}
	
	private void execute(List<AspectAdviceRule> aspectAdviceRuleList) throws ActionExecutionException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			Executable action = aspectAdviceRule.getExecutableAction();
			
			if(action.getActionType() == ActionType.BEAN && aspectAdviceRule.getAdviceBeanId() != null) {
				Object adviceBean = translet.getAspectAdviceBean(aspectAdviceRule.getAspectId());
				
				if(adviceBean == null)
					adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
				
				logger.debug("adviceBean [" + adviceBean + "] aspectAdviceRule " + aspectAdviceRule);
				translet.putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
			}

			try {
				Object adviceActionResult = action.execute(this);
				
				if(adviceActionResult != null && adviceActionResult != ActionResult.NO_RESULT) {
					logger.debug("adviceActionResult [" + adviceActionResult + "] aspectAdviceRule " + aspectAdviceRule);
					translet.putAdviceResult(aspectAdviceRule, adviceActionResult);
				}
			} catch(Exception e) {
				setRaisedException(e);
				throw new ActionExecutionException("action execution error", e);
			}
		}
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
		
		responseEnd();
	}
	
	/**
	 * Forwarding.
	 *
	 * @throws ResponseException the active response exception
	 */
	private void forward() throws CoreActivityException {
		if(debugEnabled) {
			logger.debug("> forwarding for translet '" + forwardTransletName + "'");
		}
		
		ProcessResult processResult = translet.getProcessResult();
		init(forwardTransletName);
		run(processResult);
	}
	
	private void responseByContentType(List<AspectAdviceRule> aspectAdviceRuleList) throws CoreActivityException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = aspectAdviceRule.getResponseByContentTypeRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(isResponseEnd)
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
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.Activity#getForwardTransletName()
	 */
	public String getForwardTransletName() {
		return forwardTransletName;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.activity.Activity#setForwardTransletName(java.lang.String)
	 */
	public void setForwardTransletName(String forwardTransletName) {
		this.forwardTransletName = forwardTransletName;
	}
	
	/**
	 * 응답 종료.
	 */
	public void responseEnd() {
		//if(debugEnabled) {
		//	logger.debug("response terminated");
		//}
		
		isResponseEnd = true;
	}
	
	/**
	 * 응답 종료 여부 반환.
	 * 
	 * @return true, if checks if is response end
	 */
	public boolean isResponseEnd() {
		return isResponseEnd;
	}

	/**
	 * Checks if is exception raised.
	 *
	 * @return true, if is exception raised
	 */
	public boolean isExceptionRaised() {
		return (raisedException != null);
	}
	
	public Exception getRaisedException() {
		return raisedException;
	}

	public void setRaisedException(Exception raisedException) {
		if(this.raisedException == null) {
			logger.error("original raised exception:", raisedException);
			this.raisedException = raisedException;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getAspectranContext()
	 */
	public AspectranContext getAspectranContext() {
		return context;
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
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getBean(java.lang.String)
	 */
	public Object getBean(String id) {
		return context.getBeanRegistry().getBean(id, this);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#newAspectranActivity()
	 */
	public abstract CoreActivity newCoreActivity();
	
	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return context.getApplicationAdapter();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getTransletName()
	 */
	public String getTransletName() {
		return transletName;
	}
	
	/**
	 * Sets the translet name.
	 *
	 * @param transletName the new translet name
	 */
	protected void setTransletName(String transletName) {
		this.transletName = transletName;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getRequestScope()
	 */
	public RequestScope getRequestScope() {
		return requestScope;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#setRequestScope(com.aspectran.core.context.bean.scope.RequestScope)
	 */
	public void setRequestScope(RequestScope requestScope) {
		this.requestScope = requestScope;
	}

	/**
	 * Gets the translet rule.
	 *
	 * @return the translet rule
	 */
	public TransletRule getTransletRule() {
		return transletRule;
	}

	/**
	 * Sets the translet rule.
	 *
	 * @param transletRule the new translet rule
	 */
	public void setTransletRule(TransletRule transletRule) {
		this.transletRule = transletRule;
	}

	/**
	 * Gets the request rule.
	 *
	 * @return the request rule
	 */
	public RequestRule getRequestRule() {
		return requestRule;
	}

	/**
	 * Sets the request rule.
	 *
	 * @param requestRule the new request rule
	 */
	public void setRequestRule(RequestRule requestRule) {
		this.requestRule = requestRule;
	}

	/**
	 * Gets the response rule.
	 *
	 * @return the response rule
	 */
	public ResponseRule getResponseRule() {
		return responseRule;
	}

	/**
	 * Sets the response rule.
	 *
	 * @param responseRule the new response rule
	 */
	public void setResponseRule(ResponseRule responseRule) {
		this.responseRule = responseRule;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.AspectranActivity#getBeanRegistry()
	 */
	public BeanRegistry getBeanRegistry() {
		return context.getBeanRegistry();
	}

	public Object getRequestSetting(String settingName) {
		return getSetting(requestRule.getAspectAdviceRuleRegistry(), settingName);
	}
	
	public Object getResponseSetting(String settingName) {
		return getSetting(responseRule.getAspectAdviceRuleRegistry(), settingName);
	}
	
	public Object getTransletSetting(String settingName) {
		return getSetting(transletRule.getAspectAdviceRuleRegistry(), settingName);
	}
	
	private Object getSetting(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, String settingName) {
		if(aspectAdviceRuleRegistry != null)
			return aspectAdviceRuleRegistry.getSetting(settingName);
		
		return null;
	}
	
	public Object getAspectAdviceBean(String aspectId) {
		return translet.getAspectAdviceBean(aspectId);
	}
	
}
