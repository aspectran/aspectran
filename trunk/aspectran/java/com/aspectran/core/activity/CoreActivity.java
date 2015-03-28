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

import java.util.ArrayList;
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
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.translet.TransletNotFoundException;

/**
 * Action Translator.
 * processes the active request and response.
 * 
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class CoreActivity extends AbstractActivity implements Activity {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(CoreActivity.class);
	
	private static final boolean debugEnabled = logger.isDebugEnabled();

	private static final boolean traceEnabled = logger.isTraceEnabled();

	/** The activity context. */
	private final ActivityContext context;

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
	
	/** The translet. */
	private Translet translet;
	
	/** Whether the response was ended. */
	private boolean activityEnded;

	private Exception raisedException;

	private AspectAdviceRuleRegistry transletAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry requestAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry responseAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry contentAspectAdviceRuleRegistry;
	
	/**
	 * Instantiates a new action translator.
	 *
	 * @param context the translets context
	 */
	public CoreActivity(ActivityContext context) {
		super(context.getApplicationAdapter());
		this.context = context;
	}

	public void ready(String transletName) throws ActivityException {
		ready(transletName, null);
	}
	
	protected void ready(String transletName, ProcessResult processResult) throws ActivityException {
		TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule(transletName);

		if(transletRule == null) {
			logger.debug("translet not found: {}", transletRule);
			throw new TransletNotFoundException(transletName);
		}
		
		if(debugEnabled) {
			logger.debug("translet {}", transletRule);
		}

		if(transletRule.getTransletInterfaceClass() != null)
			setTransletInterfaceClass(transletRule.getTransletInterfaceClass());

		if(transletRule.getTransletImplementClass() != null)
			setTransletImplementClass(transletRule.getTransletImplementClass());

		newTranslet();
		
		if(processResult != null)
			translet.setProcessResult(processResult);

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
			throw new ActivityException("AspectAdviceRuleRegistry clone failed.", e);
		}

		context.saveLocalCoreActivity(this);
	}
	
	public void perform() throws ActivityException {
		try {
			withoutResponse = false;
			
			run1st();
		} catch(Exception e) {
			throw new ActivityException("aspecran activity run error", e);
		}
	}
	
	public void performWithoutResponse() throws ActivityException {
		try {
			withoutResponse = true;
			
			run1st();
		} catch(Exception e) {
			throw new ActivityException("aspecran activity run error without response", e);
		}
	}
	
	private void run1st() throws ActivityException {
		try {
			try {
				// execute Before Advice Action for Translet Joinpoint
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = transletAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(activityEnded)
					return;
				
				run2nd();
				
				if(activityEnded)
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
						forceExecute(finallyAdviceRuleList);
				}
			}
		} catch(ActivityException e) {
			setRaisedException(e);
			
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = transletRule.getExceptionHandlingRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(activityEnded) {
					return;
				}
			}
		
			if(transletAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = transletAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(activityEnded) {
						return;
					}
				}
			}
			
			throw e;
		} finally {
			if(getRequestScope() != null) {
				getRequestScope().destroy();
			}
		}
	}

	private void run2nd() throws ActivityException {
		//request
		setCurrentJoinpointScope(JoinpointScopeType.REQUEST);
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = requestAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(activityEnded)
					return;
				
				request();
				
				if(activityEnded)
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
						forceExecute(finallyAdviceRuleList);
				}
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(requestAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = requestAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(activityEnded) {
						return;
					}
				}
			}
				
			throw new RequestException("request error", e);
		}
		
		if(activityEnded)
			return;
		
		//content
		setCurrentJoinpointScope(JoinpointScopeType.CONTENT);
		
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
					
					if(activityEnded)
						return;
					
					process();
					
					if(activityEnded)
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
							forceExecute(finallyAdviceRuleList);
					}
				}
			} catch(Exception e) {
				setRaisedException(e);
				
				if(contentAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> exceptionRaizedAdviceRuleList = contentAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
					
					if(exceptionRaizedAdviceRuleList != null) {
						responseByContentType(exceptionRaizedAdviceRuleList);

						if(activityEnded) {
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
		
		if(activityEnded || withoutResponse)
			return;
		
		//response
		setCurrentJoinpointScope(JoinpointScopeType.RESPONSE);
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = responseAspectAdviceRuleRegistry.getBeforeAdviceRuleList();

					if(beforeAdviceRuleList != null)
						execute(beforeAdviceRuleList);
				}
				
				if(activityEnded)
					return;
				
				response();
				
				if(activityEnded)
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
						forceExecute(finallyAdviceRuleList);
				}
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(responseAspectAdviceRuleRegistry != null) {
				List<AspectAdviceRule> exceptionRaizedAdviceRuleList = responseAspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
				
				if(exceptionRaizedAdviceRuleList != null) {
					responseByContentType(exceptionRaizedAdviceRuleList);
					
					if(activityEnded) {
						return;
					}
				}
			}
			
			throw new ResponseException("response error", e);
		}
	}
	
	private void request() throws RequestException {
		request(translet);
	}
	
	protected void request(Translet translet) throws RequestException {
	}
	
	private ProcessResult process() throws ActivityException {
		// execute action on contents area
		ContentList contentList = transletRule.getContentList();

		if(contentList != null) {
			for(ActionList actionList : contentList) {
				execute(actionList);
				
				if(activityEnded)
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
	public Response getResponse() {
		if(responseRule == null)
			return null;

		return responseRule.getResponse();
	}
	
	private void response() throws ActivityException {
		Response res = getResponse();
		
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
	public void response(Response res) throws ResponseException {
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
	private void forward() throws ActivityException {
		if(debugEnabled) {
			logger.debug("forwarding for translet: {}", forwardTransletName);
		}
		
		ProcessResult processResult = translet.getProcessResult();
		ready(forwardTransletName, processResult);
		perform();
	}
	
	public void responseByContentType(List<AspectAdviceRule> aspectAdviceRuleList) throws ActivityException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			ResponseByContentTypeRuleMap responseByContentTypeRuleMap = aspectAdviceRule.getResponseByContentTypeRuleMap();
			
			if(responseByContentTypeRuleMap != null) {
				responseByContentType(responseByContentTypeRuleMap);
				
				if(activityEnded)
					return;
			}
		}
	}

	private void responseByContentType(ResponseByContentTypeRuleMap responseByContentTypeRuleMap) throws ActivityException {
		ResponseByContentTypeRule rbctr = responseByContentTypeRuleMap.getResponseByContentTypeRule(getRaisedException());
		
		if(rbctr != null) {
			logger.info("raised exception: {}", getRaisedException().toString());
			responseByContentType(rbctr);
		}
	}

	/**
	 * Response by content type.
	 *
	 * @param responseByContentTypeRule the response by content type rule
	 * @throws ResponseException 
	 * @throws ProcessException 
	 * @throws RequestException 
	 */
	private void responseByContentType(ResponseByContentTypeRule responseByContentTypeRule) throws ActivityException {
		Response response = getResponse();

		if(response != null && response.getContentType() != null) {
			Response response2 = responseByContentTypeRule.getResponse(response.getContentType());
			ResponseRule newResponseRule = responseRule.newResponseRule(response2);
			responseRule = newResponseRule;
			
			logger.info("response by content-type: {}", responseRule);

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
	
	/**
	 * Execute.
	 *
	 * @param actionList the action list
	 * @throws ActivityException 
	 */
	protected void execute(ActionList actionList) throws ActivityException {
		ContentResult contentResult = null;
		
		if(!actionList.isHidden()) {
			contentResult = new ContentResult();
			contentResult.setName(actionList.getName());
			contentResult.setContentId(actionList.getContentId());
			contentResult.setOmittable(actionList.isOmittable());

			ContentList contentList = actionList.getParent();
			String contentsName = contentList != null ? contentList.getName() : null;
			
			ProcessResult processResult = translet.touchProcessResult(contentsName);
			processResult.addContentResult(contentResult);
			
			if(contentList != null && contentList.isOmittable())
				processResult.setOmittable(true);
		}

		for(Executable action : actionList) {
			execute(action, contentResult);
			
			if(activityEnded)
				break;
		}
	}
	
	private void execute(Executable action, ContentResult contentResult) throws ActionExecutionException {
		if(debugEnabled)
			logger.debug("action {}", action.toString());
		
		try {
			Object resultValue = action.execute(this);
		
			if(contentResult != null && !action.isHidden() && resultValue != ActionResult.NO_RESULT) {
				contentResult.addActionResult(action.getActionId(), resultValue);
			}
			
			if(traceEnabled)
				logger.debug("actionResult {}", resultValue);
		} catch(Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("action execution error", e);
		}
	}
	
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList) throws ActionExecutionException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule);

			if(activityEnded)
				return;
		}
	}
	
	public void forceExecute(List<AspectAdviceRule> aspectAdviceRuleList) throws ActionExecutionException {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule);
		}
	}
	
	private Object execute(AspectAdviceRule aspectAdviceRule) throws ActionExecutionException {
		Executable action = aspectAdviceRule.getExecutableAction();
		
		if(action == null) {
			//logger.error("no specified action on AspectAdviceRule " + aspectAdviceRule);
			//return null;
			throw new ActionExecutionException("No specified action on AspectAdviceRule " + aspectAdviceRule);
		}
		
		if(action.getActionType() == ActionType.BEAN && aspectAdviceRule.getAdviceBeanId() != null) {
			Object adviceBean = translet.getAspectAdviceBean(aspectAdviceRule.getAspectId());
			
			if(adviceBean == null)
				adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
			
			if(debugEnabled)
				logger.debug("aspectAdvice {} {}", aspectAdviceRule, adviceBean);
			
			translet.putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
		}
		
		try {
			//if(debugEnabled)
			//	logger.debug("action {}", action);

			Object adviceActionResult = action.execute(this);
			
			if(adviceActionResult != null && adviceActionResult != ActionResult.NO_RESULT) {
				translet.putAdviceResult(aspectAdviceRule, adviceActionResult);
			}
			
			if(traceEnabled)
				logger.trace("adviceActionResult {}", adviceActionResult);
			
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
			logger.error("original raised exception: ", raisedException);
			this.raisedException = raisedException;
		}
	}
	
	public void activityEnd() {
		activityEnded = true;
	}
	
	/**
	 * 
	 * @return true, if checks if is response end
	 */
	public boolean isActivityEnded() {
		return activityEnded;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		Activity activity = new CoreActivity(getActivityContext());
		return (T)activity;
	}
	
	protected void newTranslet() {
		translet = newTranslet(this);
	}
	
	public Translet getTranslet() {
		return translet;
	}

	public ProcessResult getProcessResult() {
		if(translet == null)
			return null;
		
		return translet.getProcessResult();
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

	public <T> T getTransletSetting(String settingName) {
		return getSetting(transletAspectAdviceRuleRegistry, settingName);
	}

	public <T> T getRequestSetting(String settingName) {
		return getSetting(requestAspectAdviceRuleRegistry, settingName);
	}

	public <T> T getResponseSetting(String settingName) {
		return getSetting(responseAspectAdviceRuleRegistry, settingName);
	}

	private <T> T getSetting(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, String settingName) {
		if(aspectAdviceRuleRegistry != null)
			return aspectAdviceRuleRegistry.getSetting(settingName);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#getActivityContext()
	 */
	public ActivityContext getActivityContext() {
		return context;
	}

	public BeanRegistry getBeanRegistry() {
		return context.getContextBeanRegistry();
	}
	
	public <T> T getBean(String id) {
		return context.getContextBeanRegistry().getBean(id);
	}
	
	public void registerAspectRule(AspectRule aspectRule) throws ActionExecutionException {
		if(debugEnabled)
			logger.debug("registerAspectRule {}", aspectRule);
		
		JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
		
		if(joinpointScope == JoinpointScopeType.TRANSLET || getCurrentJoinpointScope() == joinpointScope) {
			if(JoinpointScopeType.TRANSLET == joinpointScope) {
				if(transletAspectAdviceRuleRegistry == null)
					transletAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(transletAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.REQUEST == joinpointScope) {
				if(requestAspectAdviceRuleRegistry == null)
					requestAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(requestAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.RESPONSE == joinpointScope) {
				if(responseAspectAdviceRuleRegistry == null)
					responseAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(responseAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.CONTENT == joinpointScope) {
				if(contentAspectAdviceRuleRegistry == null)
					contentAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				AspectAdviceRuleRegister.register(contentAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
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
	
	public <T> T getAspectAdviceBean(String aspectId) {
		return translet.getAspectAdviceBean(aspectId);
	}
	
	public void finish() {
		context.clearLocalCoreActivity();
	}
	
}
