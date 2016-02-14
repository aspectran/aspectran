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

import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.ProcessException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegister;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.template.TemplateProcessor;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CoreActivity.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class CoreActivity extends AbstractActivity implements Activity {

	private static final Log log = LogFactory.getLog(CoreActivity.class);
	
	private static final boolean debugEnabled = log.isDebugEnabled();

	private static final boolean traceEnabled = log.isTraceEnabled();

	private final ActivityContext context;

	private TransletRule transletRule;
	
	private RequestRule requestRule;
	
	private ResponseRule responseRule;
	
	private String transletName;
	
	private String forwardTransletName;

	private boolean withoutResponse;

	private RequestMethodType requestMethod;
	
	private Translet translet;
	
	/** Whether the current activity is completed or interrupted. */
	private boolean activityEnded;

	private Exception raisedException;

	private AspectAdviceRuleRegistry transletAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry requestAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry responseAspectAdviceRuleRegistry;
	
	private AspectAdviceRuleRegistry contentAspectAdviceRuleRegistry;
	
	/**
	 * Instantiates a new CoreActivity.
	 *
	 * @param context the current ActivityContext
	 */
	public CoreActivity(ActivityContext context) {
		super(context.getApplicationAdapter());
		this.context = context;
	}

	@Override
	public void ready(String transletName) {
		ready(transletName, (ProcessResult)null);
	}

	@Override
	public void ready(String transletName, String requestMethod) {
		this.requestMethod = RequestMethodType.lookup(requestMethod);
		ready(transletName, (ProcessResult)null);
	}

	@Override
	public void ready(String transletName, RequestMethodType requestMethod) {
		this.requestMethod = requestMethod;
		ready(transletName, (ProcessResult)null);
	}

	private void ready(String transletName, ProcessResult processResult) {
		try {
			TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule(transletName);

			// for RESTful
			PathVariableMap pathVariableMap = null;
			if(transletRule == null && requestMethod != null) {
				pathVariableMap = context.getTransletRuleRegistry().getPathVariableMap(transletName, requestMethod);
				if(pathVariableMap != null) {
					transletRule = pathVariableMap.getTransletRule();
				}
			}

			if(transletRule == null) {
				throw new TransletNotFoundException(transletName);
			}

			if(debugEnabled) {
				log.debug("translet " + transletRule);
			}

			if(transletRule.getTransletInterfaceClass() != null)
				setTransletInterfaceClass(transletRule.getTransletInterfaceClass());

			if(transletRule.getTransletImplementClass() != null)
				setTransletImplementClass(transletRule.getTransletImplementClass());

			newTranslet();

			if(processResult != null) {
				translet.setProcessResult(processResult);
			}

			this.transletName = transletName;
			this.transletRule = transletRule;
			this.requestRule = transletRule.getRequestRule();
			this.responseRule = transletRule.getResponseRule();

			this.transletAspectAdviceRuleRegistry = transletRule.getAspectAdviceRuleRegistry(true);
			this.requestAspectAdviceRuleRegistry = requestRule.getAspectAdviceRuleRegistry(true);
			this.responseAspectAdviceRuleRegistry = responseRule.getAspectAdviceRuleRegistry(true);
			if(transletRule.getContentList() != null) {
				this.contentAspectAdviceRuleRegistry = transletRule.getContentList().getAspectAdviceRuleRegistry(true);
			}

			context.setCurrentActivity(this);

			adapting();

			if(pathVariableMap != null) {
				pathVariableMap.apply(translet);
			}
		} catch(TransletNotFoundException e) {
			throw e;
		} catch(Exception e) {
			throw new ActivityException("Failed to ready for Web Activity.", e);
		}
	}

	protected void adapting() throws AdaptingException {
	}

	@Override
	public void perform() {
		run1st();
	}

	@Override
	public void performWithoutResponse() {
		withoutResponse = true;
		run1st();
	}

	@Override
	public void finish() {
		context.removeCurrentActivity();
	}
	
	private void run1st() {
		try {
			try {
				// execute Before Advice Action for Translet Joinpoint
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = transletAspectAdviceRuleRegistry.getBeforeAdviceRuleList();
					if(beforeAdviceRuleList != null) {
						execute(beforeAdviceRuleList);
					}
				}
				
				if(!activityEnded) {
					run2nd();
				}

				// execute After Advice Action for Translet Joinpoint
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = transletAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					if(afterAdviceRuleList != null) {
						execute(afterAdviceRuleList);
					}
				}

			} finally {
				if(transletAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = transletAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					if(finallyAdviceRuleList != null) {
						forceExecute(finallyAdviceRuleList);
					}
				}

				if(getRequestScope() != null) {
					getRequestScope().destroy();
				}
			}
		} catch(RequestMethodNotAllowedException e) {
			throw e;
		} catch(Exception e) {
			setRaisedException(e);
			
			ExceptionHandlingRule exceptionHandlingRule = transletRule.getExceptionHandlingRule();
			if(exceptionHandlingRule != null) {
				responseByContentType(exceptionHandlingRule);
				if(activityEnded) {
					return;
				}
			}
			
			if(transletAspectAdviceRuleRegistry != null) {
				List<ExceptionHandlingRule> exceptionHandlingRuleList = transletAspectAdviceRuleRegistry.getExceptionHandlingRuleList();
				if(exceptionHandlingRuleList != null) {
					responseByContentType(exceptionHandlingRuleList);
					if(activityEnded) {
						return;
					}
				}
			}
			
			throw new ActivityException("Failed to run activity.", e);
		} finally {
			if(getRequestScope() != null) {
				getRequestScope().destroy();
			}
		}
	}

	private void run2nd() {
		//request
		setCurrentJoinpointScope(JoinpointScopeType.REQUEST);
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = requestAspectAdviceRuleRegistry.getBeforeAdviceRuleList();
					if(beforeAdviceRuleList != null) {
						execute(beforeAdviceRuleList);
					}
				}
				
				if(!activityEnded) {
					request();
				}

				// execute After Advice Action for Request Joinpoint
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = requestAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					if(afterAdviceRuleList != null) {
						execute(afterAdviceRuleList);
					}
				}
				
				if(activityEnded) {
					return;
				}

			} finally {
				if(requestAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = requestAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					if(finallyAdviceRuleList != null) {
						forceExecute(finallyAdviceRuleList);
					}
				}
			}
		} catch(RequestMethodNotAllowedException e) {
			throw e;
		} catch(Exception e) {
			setRaisedException(e);
			
			if(requestAspectAdviceRuleRegistry != null) {
				List<ExceptionHandlingRule> exceptionHandlingRuleList = requestAspectAdviceRuleRegistry.getExceptionHandlingRuleList();
				if(exceptionHandlingRuleList != null) {
					responseByContentType(exceptionHandlingRuleList);
					if(activityEnded) {
						return;
					}
				}
			}
				
			throw new RequestException("Request processing failed.", e);
		}
		
		if(activityEnded) {
			return;
		}
		
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
					
					if(!activityEnded) {
						process();
					}
					
					// execute After Advice Action for Content Joinpoint
					if(contentAspectAdviceRuleRegistry != null) {
						List<AspectAdviceRule> afterAdviceRuleList = contentAspectAdviceRuleRegistry.getAfterAdviceRuleList();
						
						if(afterAdviceRuleList != null)
							execute(afterAdviceRuleList);
					}

					if(activityEnded) {
						return;
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
					List<ExceptionHandlingRule> exceptionHandlingRuleList = contentAspectAdviceRuleRegistry.getExceptionHandlingRuleList();
					if(exceptionHandlingRuleList != null) {
						responseByContentType(exceptionHandlingRuleList);
						if(activityEnded) {
							return;
						}
					}
				}
				
				throw new ProcessException("Content processing failed.", e);
			}
		}
		
		if(activityEnded || withoutResponse) {
			return;
		}
		
		//response
		setCurrentJoinpointScope(JoinpointScopeType.RESPONSE);
		
		try {
			try {
				// execute Before Advice Action for Request Joinpoint
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> beforeAdviceRuleList = responseAspectAdviceRuleRegistry.getBeforeAdviceRuleList();
					if(beforeAdviceRuleList != null) {
						execute(beforeAdviceRuleList);
					}
				}
				
				if(!activityEnded) {
					response();
				}
				
				// execute After Advice Action for Request Joinpoint
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> afterAdviceRuleList = responseAspectAdviceRuleRegistry.getAfterAdviceRuleList();
					if(afterAdviceRuleList != null) {
						execute(afterAdviceRuleList);
					}
				}

				if(activityEnded) {
					return;
				}

			} finally {
				if(responseAspectAdviceRuleRegistry != null) {
					List<AspectAdviceRule> finallyAdviceRuleList = responseAspectAdviceRuleRegistry.getFinallyAdviceRuleList();
					if(finallyAdviceRuleList != null) {
						forceExecute(finallyAdviceRuleList);
					}
				}
			}
		} catch(Exception e) {
			setRaisedException(e);
			
			if(responseAspectAdviceRuleRegistry != null) {
				List<ExceptionHandlingRule> exceptionHandlingRuleList = responseAspectAdviceRuleRegistry.getExceptionHandlingRuleList();
				if(exceptionHandlingRuleList != null) {
					responseByContentType(exceptionHandlingRuleList);
					if(activityEnded) {
						return;
					}
				}
			}
			
			throw new ResponseException("Response processing failed.", e);
		}
	}

	@Override
	public String determineRequestCharacterEncoding() {
		String characterEncoding = requestRule.getCharacterEncoding();

		if(characterEncoding == null)
			characterEncoding = getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);

		return characterEncoding;
	}

	@Override
	public String determineResponseCharacterEncoding() {
		String characterEncoding = responseRule.getCharacterEncoding();

		if(characterEncoding == null)
			characterEncoding = getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);

		if(characterEncoding == null)
			characterEncoding = determineRequestCharacterEncoding();

		return characterEncoding;
	}

	protected void request() {
	}
	
	private ProcessResult process() {
		// execute action on contents area
		ContentList contentList = transletRule.getContentList();

		if(contentList != null) {
			ProcessResult processResult = null;
			
			for(ActionList actionList : contentList) {
				if(!actionList.isHidden()) {
					processResult = translet.touchProcessResult(contentList.getName());
					if(contentList.isOmittable())
						processResult.setOmittable(true);
					break;
				}
			}
			
			for(ActionList actionList : contentList) {
				execute(actionList);
				if(activityEnded)
					break;
			}
		}
		
		return translet.getProcessResult();
	}

	@Override
	public ProcessResult getProcessResult() {
		return translet.getProcessResult();
	}

	@Override
	public Object getProcessResult(String actionId) {
		return translet.getProcessResult().getResultValue(actionId);
	}

	@Override
	public Response getResponse() {
		if(responseRule == null)
			return null;

		return responseRule.getResponse();
	}
	
	private void response() {
		Response res = getResponse();
		
		if(res != null)
			response(res);
		
		if(forwardTransletName != null)
			forward();
	}

	@Override
	public void response(Response response) {
		response.response(this);
		
		if(response.getResponseType() == ResponseType.FORWARD) {
			ForwardResponse forwardResponse = (ForwardResponse)response;
			String forwardTransletName = forwardResponse.getForwardResponseRule().getTransletName();
			setForwardTransletName(forwardTransletName);
		}
		
		activityEnd();
	}
	
	/**
	 * Forwards to other translet.
	 */
	private void forward() {
		if(debugEnabled) {
			log.debug("Forwarding to Translet [" + forwardTransletName + "]");
		}
		
		ProcessResult processResult = translet.getProcessResult();
		ready(forwardTransletName, processResult);
		perform();
	}

	@Override
	public void responseByContentType(List<ExceptionHandlingRule> exceptionHandlingRuleList) {
		for(ExceptionHandlingRule exceptionHandlingRule : exceptionHandlingRuleList) {
			responseByContentType(exceptionHandlingRule);
			if(activityEnded)
				return;
		}
	}

	private void responseByContentType(ExceptionHandlingRule exceptionHandlingRule) {
		ResponseByContentTypeRule rbctr = exceptionHandlingRule.getResponseByContentTypeRule(getRaisedException());
		if(rbctr != null) {
			log.info("Raised exception: " + getRaisedException());
			responseByContentType(rbctr);
		}
	}

	/**
	 * Response by content type.
	 *
	 * @param responseByContentTypeRule the response by content type rule
	 */
	private void responseByContentType(ResponseByContentTypeRule responseByContentTypeRule) {
		Response response = getResponse();

		if(response != null && response.getContentType() != null) {
			Response response2 = responseByContentTypeRule.getResponse(response.getContentType());
			ResponseRule urgentResponseRule = responseRule.newUrgentResponseRule(response2);
			responseRule = urgentResponseRule;
			
			log.info("response by content-type: " + responseRule);

			// Clear process results. No reflection to ProcessResult.
			translet.setProcessResult(null);
			
			if(responseRule.getResponse() != null) {
				ActionList actionList = responseRule.getResponse().getActionList();
				if(actionList != null) {
					execute(actionList);
				}
				response();
			}
		}
	}

	@Override
	public String getForwardTransletName() {
		return forwardTransletName;
	}

	/**
	 * Specify the forwarding destination translet name.
	 *
	 * @param forwardTransletName the new forwarding destination translet name
	 */
	protected void setForwardTransletName(String forwardTransletName) {
		this.forwardTransletName = forwardTransletName;
	}
	
	/**
	 * Execute the actions.
	 *
	 * @param actionList the action list
	 * @throws ActivityException the activity exception
	 */
	protected void execute(ActionList actionList) {
		ContentResult contentResult = null;
		
		for(Executable action : actionList) {
			contentResult = new ContentResult(translet.getProcessResult());
			contentResult.setName(actionList.getName());
			contentResult.setOmittable(actionList.isOmittable());

			execute(action, contentResult);
			
			if(activityEnded)
				break;
		}
	}
	
	private void execute(Executable action, ContentResult contentResult) {
		if(debugEnabled)
			log.debug("action " + action);
		
		try {
			Object resultValue = action.execute(this);
		
			if(contentResult != null && !action.isHidden() && resultValue != ActionResult.NO_RESULT) {
				ActionResult actionResult = new ActionResult(contentResult);
				actionResult.setActionId(action.getActionId());
				actionResult.setResultValue(resultValue);
			}
			
			if(traceEnabled)
				log.trace("actionResult " + resultValue);
		} catch(Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("Failed to execute action " + action, e);
		}
	}

	@Override
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList) {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule, false);
		}
	}

	@Override
	public void forceExecute(List<AspectAdviceRule> aspectAdviceRuleList) {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule, true);
		}
	}
	
	private void execute(AspectAdviceRule aspectAdviceRule, boolean force) {
		try {
			Executable action = aspectAdviceRule.getExecutableAction();
			
			if(action == null) {
				throw new IllegalArgumentException("No specified action on AspectAdviceRule " + aspectAdviceRule);
			}
			
			if(action.getActionType() == ActionType.BEAN && aspectAdviceRule.getAdviceBeanId() != null) {
				Object adviceBean = translet.getAspectAdviceBean(aspectAdviceRule.getAspectId());
				
				if(adviceBean == null) {
					if(aspectAdviceRule.getAdviceBeanClass() != null)
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanClass());
					else
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
				}
				
				translet.putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
			}
			
			Object adviceActionResult = action.execute(this);
			
			if(adviceActionResult != null && adviceActionResult != ActionResult.NO_RESULT) {
				translet.putAdviceResult(aspectAdviceRule, adviceActionResult);
			}
			
			if(traceEnabled)
				log.trace("adviceActionResult " + adviceActionResult);
		} catch(Exception e) {
			setRaisedException(e);
			
			if(!force) {
				throw new ActionExecutionException("Failed to execute advice action " + aspectAdviceRule, e);
			} else {
				log.error("Failed to execute advice action " + aspectAdviceRule, e);
			}
		}
	}

	@Override
	public boolean isExceptionRaised() {
		return (raisedException != null);
	}

	@Override
	public Exception getRaisedException() {
		return raisedException;
	}

	@Override
	public void setRaisedException(Exception raisedException) {
		if(this.raisedException == null) {
			log.error("original raised exception: ", raisedException);
			this.raisedException = raisedException;
		}
	}

	@Override
	public void activityEnd() {
		activityEnded = true;
	}

	@Override
	public boolean isActivityEnded() {
		return activityEnded;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		Activity activity = new CoreActivity(getActivityContext());
		return (T)activity;
	}
	
	/**
	 * Create a new translet.
	 */
	protected void newTranslet() {
		translet = newTranslet(this);
	}

	@Override
	public Translet getTranslet() {
		return translet;
	}

	@Override
	public String getTransletName() {
		return transletName;
	}

	@Override
	public RequestMethodType getRequestMethod() {
		return requestMethod;
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

	@Override
	public <T> T getTransletSetting(String settingName) {
		return getSetting(transletAspectAdviceRuleRegistry, settingName);
	}

	@Override
	public <T> T getRequestSetting(String settingName) {
		return getSetting(requestAspectAdviceRuleRegistry, settingName);
	}

	@Override
	public <T> T getResponseSetting(String settingName) {
		return getSetting(responseAspectAdviceRuleRegistry, settingName);
	}

	private <T> T getSetting(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, String settingName) {
		if(aspectAdviceRuleRegistry != null)
			return aspectAdviceRuleRegistry.getSetting(settingName);
		
		return null;
	}

	@Override
	public ActivityContext getActivityContext() {
		return context;
	}

	@Override
	public BeanRegistry getBeanRegistry() {
		return context.getBeanRegistry();
	}

	@Override
	public TemplateProcessor getTemplateProcessor() {
		return context.getTemplateProcessor();
	}

	@Override
	public <T> T getBean(String id) {
		return context.getBeanRegistry().getBean(id);
	}

	@Override
	public <T> T getBean(Class<T> classType) {
		return context.getBeanRegistry().getBean(classType);
	}

	@Override
	public <T> T getBean(String id, Class<T> classType) {
		return context.getBeanRegistry().getBean(id, classType);
	}

	@Override
	public void registerAspectRule(AspectRule aspectRule) {
		if(debugEnabled)
			log.debug("register AspectRule " + aspectRule);
		
		JoinpointScopeType joinpointScope = aspectRule.getJoinpointScope();
		
		/*
		 * before-advice is excluded because it is already processed.
		 */
		if(joinpointScope == JoinpointScopeType.TRANSLET || getCurrentJoinpointScope() == joinpointScope) {
			if(JoinpointScopeType.TRANSLET == joinpointScope) {
				if(transletAspectAdviceRuleRegistry == null) {
					transletAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				}
				AspectAdviceRuleRegister.register(transletAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.REQUEST == joinpointScope) {
				if(requestAspectAdviceRuleRegistry == null) {
					requestAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				}
				AspectAdviceRuleRegister.register(requestAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.RESPONSE == joinpointScope) {
				if(responseAspectAdviceRuleRegistry == null) {
					responseAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				}
				AspectAdviceRuleRegister.register(responseAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			} else if(JoinpointScopeType.CONTENT == joinpointScope) {
				if(contentAspectAdviceRuleRegistry == null) {
					contentAspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				}
				AspectAdviceRuleRegister.register(contentAspectAdviceRuleRegistry, aspectRule, AspectAdviceType.BEFORE);
			}
			
			List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
			
			if(aspectAdviceRuleList != null) {
				for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
					if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
						execute(aspectAdviceRule, false);
					}
				}
			}
		}
	}

	@Override
	public <T> T getAspectAdviceBean(String aspectId) {
		return translet.getAspectAdviceBean(aspectId);
	}
	
}
