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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.activity.response.Responsible;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.type.ActionType;
import com.aspectran.core.var.type.AspectAdviceType;
import com.aspectran.core.var.type.JoinpointScopeType;

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
	protected final ActivityContext context;
	
	/** The translet interface class. */
	protected Class<? extends CoreTranslet> transletInterfaceClass;
	
	/** The translet instance class. */
	protected Class<? extends CoreTransletImpl> transletImplementClass;

	/** The translet. */
	protected CoreTranslet translet;
	
	/** Whether the response is ended. */
	protected boolean isActivityEnd;

	private Exception raisedException;

	protected AspectAdviceRuleRegistry transletAspectAdviceRuleRegistry;
	
	protected AspectAdviceRuleRegistry requestAspectAdviceRuleRegistry;
	
	protected AspectAdviceRuleRegistry responseAspectAdviceRuleRegistry;
	
	protected AspectAdviceRuleRegistry contentAspectAdviceRuleRegistry;
	
	protected JoinpointScopeType joinpointScope = JoinpointScopeType.TRANSLET;
	
	/**
	 * Instantiates a new action translator.
	 *
	 * @param context the translets context
	 */
	public AbstractCoreActivity(ActivityContext context) {
		this.context = context;
	}

	/**
	 * Gets the translet interface class.
	 *
	 * @return the translet interface class
	 */
	public Class<? extends CoreTranslet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	/**
	 * Sets the translet interface class.
	 *
	 * @param transletInterfaceClass the new translet interface class
	 */
	protected void setTransletInterfaceClass(Class<? extends CoreTranslet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	/**
	 * Gets the translet instance class.
	 *
	 * @return the translet instance class
	 */
	public Class<? extends CoreTransletImpl> getTransletImplementClass() {
		return transletImplementClass;
	}

	/**
	 * Sets the translet instance class.
	 *
	 * @param transletInstanceClass the new translet instance class
	 */
	protected void setTransletImplementClass(Class<? extends CoreTransletImpl> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}

	public CoreTranslet getSuperTranslet() {
		return translet;
	}

	public ProcessResult getProcessResult() {
		if(translet == null)
			return null;
		
		return translet.getProcessResult();
	}
	
	/**
	 * Execute.
	 *
	 * @param actionList the action list
	 * @throws CoreActivityException 
	 */
	protected void execute(ActionList actionList) throws CoreActivityException {
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
			
			try {
				if(aspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					
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
						
						if(isActivityEnd) {
							return;
						}
					}
				}
				
				throw new ActionExecutionException("action execution error", e);
			}
			
			if(isActivityEnd)
				break;
		}
	}
	
	private void execute(Executable action, AspectAdviceRuleRegistry aspectAdviceRuleRegistry) throws ActionExecutionException {
		// execute Before Advice Action
		List<AspectAdviceRule> beforeAdviceRuleList = aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
		
		if(beforeAdviceRuleList != null) {
			execute(beforeAdviceRuleList);
		
			if(isActivityEnd)
				return;
		}
		
		execute(action);
		
		if(isActivityEnd)
			return;
		
		// execute After Advice Action
		List<AspectAdviceRule> afterAdviceRuleList = aspectAdviceRuleRegistry.getAfterAdviceRuleList();

		if(afterAdviceRuleList != null)
			execute(afterAdviceRuleList);
	}
	
	private void execute(Executable action) throws ActionExecutionException {
		if(debugEnabled)
			logger.debug("execute action " + action.toString());
		
		try {
			Object resultValue = action.execute(this);
		
			//if(debugEnabled)
			//	logger.debug("action " + action + " result: " + resultValue);
			
			if(!action.isHidden() && resultValue != ActionResult.NO_RESULT) {
				translet.addActionResult(action.getActionId(), resultValue);
			}
		} catch(Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("action execution error", e);
		}
	}
	
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList) throws ActionExecutionException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			if(isActivityEnd) {
				if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY)
					execute(aspectAdviceRule);
			} else {
				execute(aspectAdviceRule);
			}
		}
	}
	
	public Object execute(AspectAdviceRule aspectAdviceRule) throws ActionExecutionException {
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
			
			return adviceActionResult;
		} catch(Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("action execution error", e);
		}
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
	 * @see com.aspectran.core.activity.CoreActivity#getActivityContext()
	 */
	public ActivityContext getActivityContext() {
		return context;
	}

	/**
	 * Gets the application adapter.
	 *
	 * @return the application adapter
	 */
	public ApplicationAdapter getApplicationAdapter() {
		return context.getApplicationAdapter();
	}

	public BeanRegistry getBeanRegistry() {
		return context.getLocalBeanRegistry();
	}
	
	public Object getBean(String id) {
		return context.getLocalBeanRegistry().getBean(id);
	}
	
	public Object getTransletSetting(String settingName) {
		return getSetting(transletAspectAdviceRuleRegistry, settingName);
	}
	
	public Object getRequestSetting(String settingName) {
		return getSetting(requestAspectAdviceRuleRegistry, settingName);
	}
	
	public Object getResponseSetting(String settingName) {
		return getSetting(responseAspectAdviceRuleRegistry, settingName);
	}

	private Object getSetting(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, String settingName) {
		if(aspectAdviceRuleRegistry != null)
			return aspectAdviceRuleRegistry.getSetting(settingName);
		
		return null;
	}

	public void registerAspectRule(AspectRule aspectRule) throws ActionExecutionException {
		JoinpointScopeType joinpointScope2 = aspectRule.getJoinpointScope();
		
		if(this.joinpointScope == JoinpointScopeType.TRANSLET || this.joinpointScope == joinpointScope2) {
			if(JoinpointScopeType.TRANSLET == joinpointScope2) {
				if(transletAspectAdviceRuleRegistry == null)
					transletAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(transletAspectAdviceRuleRegistry, aspectRule);
			} else if(JoinpointScopeType.REQUEST == joinpointScope2) {
				if(requestAspectAdviceRuleRegistry == null)
					requestAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(requestAspectAdviceRuleRegistry, aspectRule);
			} else if(JoinpointScopeType.RESPONSE == joinpointScope2) {
				if(responseAspectAdviceRuleRegistry == null)
					responseAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(responseAspectAdviceRuleRegistry, aspectRule);
			} else if(JoinpointScopeType.CONTENT == joinpointScope2) {
				if(contentAspectAdviceRuleRegistry == null)
					contentAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(contentAspectAdviceRuleRegistry, aspectRule);
			}
			
			List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
			
			if(aspectAdviceRuleList != null) {
				List<AspectAdviceRule> beforeAdviceRuleList = null;
				
				for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
					if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
						if(beforeAdviceRuleList == null)
							beforeAdviceRuleList = new ArrayList<AspectAdviceRule>();
						
						beforeAdviceRuleList.add(aspectAdviceRule);
					}
				}
				
				if(beforeAdviceRuleList != null)
					execute(beforeAdviceRuleList);
			}
		}
	}
	
	public Object getAspectAdviceBean(String aspectId) {
		return translet.getAspectAdviceBean(aspectId);
	}
	
	public JoinpointScopeType getJoinpointScope() {
		return joinpointScope;
	}
	
	public void close() {
	}
	
	public void activityEnd() {
		isActivityEnd = true;
	}
	
	/**
	 * 
	 * @return true, if checks if is response end
	 */
	public boolean isActivityEnd() {
		return isActivityEnd;
	}

	public abstract RequestAdapter getRequestAdapter();

	public abstract ResponseAdapter getResponseAdapter();

	public abstract SessionAdapter getSessionAdapter();

	public abstract void init(String transletName) throws CoreActivityException;

	public abstract void run() throws CoreActivityException;

	public abstract void runWithoutResponse() throws CoreActivityException;

	public abstract void request() throws RequestException;

	public abstract ProcessResult process() throws CoreActivityException;

	public abstract String getForwardTransletName();

	public abstract void response(Responsible res) throws ResponseException;

	public abstract void responseByContentType(List<AspectAdviceRule> aspectAdviceRuleList) throws CoreActivityException;

	public abstract Responsible getResponse();

	public abstract String getTransletName();

	public abstract CoreActivity newCoreActivity();

	public abstract Scope getRequestScope();
	
	public abstract void setRequestScope(Scope requestScope);

}
