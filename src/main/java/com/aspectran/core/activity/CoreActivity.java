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
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ExceptionCatchRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class CoreActivity.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class CoreActivity extends AbstractActivity {

	private static final Log log = LogFactory.getLog(CoreActivity.class);
	
	private TransletRule transletRule;
	
	private RequestRule requestRule;
	
	private ResponseRule responseRule;

	private MethodType requestMethod;

	private String transletName;
	
	private String forwardTransletName;

	private boolean withoutResponse;

	private Translet translet;

	private Response penddedResponse;

	/**
	 * Instantiates a new CoreActivity.
	 *
	 * @param context the current ActivityContext
	 */
	public CoreActivity(ActivityContext context) {
		super(context);
	}
	
	@Override
	public void prepare(String transletName) {
		this.transletName = transletName;
		this.requestMethod = MethodType.GET;

		TransletRule transletRule = getTransletRuleRegistry().getTransletRule(transletName);

		if(transletRule == null) {
			throw new TransletNotFoundException(transletName);
		}

		prepare(transletRule, null);
	}

	@Override
	public void prepare(String transletName, String requestMethod) {
		prepare(transletName, MethodType.resolve(requestMethod));
	}

	@Override
	public void prepare(String transletName, MethodType requestMethod) {
		prepare(transletName, requestMethod, null);
	}

	private void prepare(String transletName, MethodType requestMethod, ProcessResult processResult) {
		this.transletName = transletName;
		this.requestMethod = (requestMethod == null) ? MethodType.GET : requestMethod;

		TransletRule transletRule = getTransletRuleRegistry().getTransletRule(transletName);

		// for RESTful
		if(transletRule == null && requestMethod != null) {
			transletRule = getTransletRuleRegistry().getRestfulTransletRule(transletName, requestMethod);
		}

		if(transletRule == null) {
			throw new TransletNotFoundException(transletName);
		}

		// for RESTful
		PathVariableMap pathVariableMap = getTransletRuleRegistry().getPathVariableMap(transletRule, transletName);

		prepare(transletRule, processResult);

		if(pathVariableMap != null) {
			pathVariableMap.apply(translet);
		}
	}

	private void prepare(TransletRule transletRule, ProcessResult processResult) {
		try {
			if(log.isDebugEnabled()) {
				log.debug("translet " + transletRule);
			}

			if(transletRule.getTransletInterfaceClass() != null)
				setTransletInterfaceClass(transletRule.getTransletInterfaceClass());

			if(transletRule.getTransletImplementationClass() != null)
				setTransletImplementationClass(transletRule.getTransletImplementationClass());

			translet = newTranslet();

			if(processResult != null) {
				translet.setProcessResult(processResult);
			}

			this.transletRule = transletRule;
			this.requestRule = transletRule.getRequestRule();
			this.responseRule = transletRule.getResponseRule();

			prepareAspectAdviceRule(transletRule);

			if(forwardTransletName == null) {
				if(isIncluded()) {
					backupCurrentActivity();
				} else {
					setCurrentActivity(this);
				}
				adapt();
				parseRequest();
			}
		} catch(Exception e) {
			throw new ActivityException("Failed to prepare activity.", e);
		}
	}

	protected void adapt() throws AdapterException {
	}

	@Override
	public void perform() {
		performTranslet();
	}

	@Override
	public void performWithoutResponse() {
		withoutResponse = true;
		performTranslet();
	}

	@Override
	public void finish() {
		removeCurrentActivity();
	}

	/**
	 * Parse the declared parameters and attributes.
	 */
	protected void parseRequest() {
		parseDeclaredParameters();
		parseDeclaredAttributes();
	}

	/**
	 * Parse the declared parameters.
	 */
	protected void parseDeclaredParameters() {
		ItemRuleMap parameterItemRuleMap = requestRule.getParameterItemRuleMap();
		if(parameterItemRuleMap != null) {
			ItemEvaluator evaluator = null;
			ItemRuleList missingItemRules = null;
			for(ItemRule itemRule : parameterItemRuleMap.values()) {
				Token[] tokens = itemRule.getTokens();
				if(tokens != null) {
					if(evaluator == null) {
						evaluator = new ItemExpressionParser(this);
					}
					String[] values = evaluator.evaluateAsStringArray(itemRule);
					String[] oldValues = getRequestAdapter().getParameterValues(itemRule.getName());
					if(values != oldValues) {
						getRequestAdapter().setParameter(itemRule.getName(), values);
					}
				}

				if(itemRule.isMandatory()) {
					String[] values = getRequestAdapter().getParameterValues(itemRule.getName());
					if(values == null) {
						if(missingItemRules == null) {
							missingItemRules = new ItemRuleList();
						}
						missingItemRules.add(itemRule);
					}
				}
			}
			if(missingItemRules != null) {
				throw new MissingMandatoryParametersException(missingItemRules);
			}
		}
	}

	/**
	 * Parse the declared attributes.
	 */
	protected void parseDeclaredAttributes() {
		ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			ItemEvaluator evaluator = new ItemExpressionParser(this);
			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				Object value = evaluator.evaluate(itemRule);
				getRequestAdapter().setAttribute(itemRule.getName(), value);
			}
		}
	}
	
	/**
	 * Perform an activity.
	 */
	private void performTranslet() {
		if(isActivityEnded()) {
			return;
		}

		try {
			try {
				// execute the Before Advice Action for Translet Joinpoint
				if(getBeforeAdviceRuleList() != null) {
					execute(getBeforeAdviceRuleList());
				}

				if(!isActivityEnded()) {
					if(transletRule.getContentList() != null) {
						produce();
					}
				}

				// execute the After Advice Action for Translet Joinpoint
				if(getAfterAdviceRuleList() != null) {
					execute(getAfterAdviceRuleList());
				}
			} catch(Exception e) {
				setRaisedException(e);
			} finally {
				if(getFinallyAdviceRuleList() != null) {
					executeWithoutThrow(getFinallyAdviceRuleList());
				}
			}

			if(isExceptionRaised()) {
				continueActivity();

				if(transletRule.getExceptionRule() != null) {
					responseByContentType(transletRule.getExceptionRule());
				}
				if(!isActivityEnded() && getExceptionRuleList() != null) {
					responseByContentType(getExceptionRuleList());
				}
			}

			if(!withoutResponse) {
				response();
			}
		} catch(Exception e) {
			throw new ActivityException("Failed to perform an activity.", e);
		} finally {
			Scope requestScope = getRequestScope(false);
			if(requestScope != null) {
				requestScope.destroy();
			}
		}
	}

	/**
	 * Produce content.
	 */
	private void produce() {
		ContentList contentList = transletRule.getContentList();

		if(contentList != null) {
			ProcessResult processResult = translet.touchProcessResult(contentList.getName(), contentList.size());

			if(transletRule.isExplicitContent()) {
				processResult.setOmittable(contentList.isOmittable());
			} else {
				if(contentList.getVisibleCount() < 2) {
					processResult.setOmittable(true);
				}
			}

			for(ActionList actionList : contentList) {
				execute(actionList);
				if(isActivityEnded())
					break;
			}
		}
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
	public Response getBaseResponse() {
		return (responseRule == null) ? null : responseRule.getResponse();
	}
	
	private void response() {
		Response res = (this.penddedResponse != null) ? this.penddedResponse : getBaseResponse();
		
		if(res != null) {
			if(res.getResponseType() != ResponseType.FORWARD) {
				getResponseAdapter().flush();
			}

			res.response(this);

			if(res.getResponseType() == ResponseType.FORWARD) {
				ForwardResponse forwardResponse = (ForwardResponse)res;
				this.forwardTransletName = forwardResponse.getForwardResponseRule().getTransletName();
			} else {
				this.forwardTransletName = null;
			}
			
			if(forwardTransletName != null) {
				forward();
			}
		}
	}

	@Override
	public void setPenddedResponse(Response response) {
		this.penddedResponse = response;
	}
	
	/**
	 * Forwards to other translet.
	 */
	private void forward() {
		if(log.isDebugEnabled()) {
			log.debug("Forwards from [" + transletName + "] to [" + forwardTransletName + "]");
		}
		
		continueActivity();
		
		prepare(forwardTransletName, requestMethod, translet.getProcessResult());
			
		perform();
	}

	@Override
	public void responseByContentType(List<ExceptionRule> exceptionRuleList) {
		for(ExceptionRule exceptionRule : exceptionRuleList) {
			responseByContentType(exceptionRule);
			if(isActivityEnded())
				return;
		}
	}

	private void responseByContentType(ExceptionRule exceptionRule) {
		ExceptionCatchRule exceptionCatchRule = exceptionRule.getExceptionCatchRule(getRaisedException());
		if(exceptionCatchRule != null) {
			log.info("Raised exception: " + getRaisedException());
			responseByContentType(exceptionCatchRule);
		}
	}

	private void responseByContentType(ExceptionCatchRule exceptionCatchRule) {
		Response response = getBaseResponse();
		Response targetResponse;

		if(response != null && response.getContentType() != null)
			targetResponse = exceptionCatchRule.getResponse(response.getContentType());
		else
			targetResponse = exceptionCatchRule.getDefaultResponse();

		if(targetResponse != null) {
			ResponseRule responseRule = new ResponseRule();
			responseRule.setResponse(targetResponse);
			if(this.responseRule != null) {
				responseRule.setCharacterEncoding(this.responseRule.getCharacterEncoding());
			}

			this.responseRule = responseRule;

			log.info("Response by Content Type " + responseRule);

			// Clear produced results. No reflection to ProcessResult.
			translet.setProcessResult(null);
			translet.touchProcessResult(null, 0).setOmittable(true);
			
			ActionList actionList = targetResponse.getActionList();
			if(actionList != null) {
				execute(actionList);
			}

			setPenddedResponse(targetResponse);
		}
	}

	/**
	 * Execute actions.
	 *
	 * @param actionList the action list
	 */
	protected void execute(ActionList actionList) {
		ContentResult contentResult = null;

		if(translet.getProcessResult() != null) {
			contentResult = new ContentResult(translet.getProcessResult(), actionList.size());
			contentResult.setName(actionList.getName());
			if(transletRule.isExplicitContent()) {
				contentResult.setOmittable(actionList.isOmittable());
			} else if(actionList.getName() == null && actionList.getVisibleCount() < 2) {
				contentResult.setOmittable(true);
			}
		}

		for(Executable action : actionList) {
			execute(action, contentResult);
			if(isActivityEnded())
				break;
		}
	}

	/**
	 * Execute action.
	 *
	 * @param action the executable action
	 * @param contentResult the content result
	 */
	private void execute(Executable action, ContentResult contentResult) {
		if(log.isDebugEnabled())
			log.debug("action " + action);
		
		try {
			Object resultValue = action.execute(this);
		
			if(contentResult != null && resultValue != ActionResult.NO_RESULT) {
				ActionResult actionResult = new ActionResult(contentResult);
				actionResult.setActionId(action.getActionId());
				actionResult.setResultValue(resultValue);
				actionResult.setHidden(action.isHidden());
			}
			
			if(log.isTraceEnabled()) {
				log.trace("actionResult " + resultValue);
			}
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
	public void executeWithoutThrow(List<AspectAdviceRule> aspectAdviceRuleList) {
		for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule, true);
		}
	}

	@Override
	public void execute(AspectAdviceRule aspectAdviceRule) {
		execute(aspectAdviceRule, false);
	}

	@Override
	public void executeWithoutThrow(AspectAdviceRule aspectAdviceRule) {
		execute(aspectAdviceRule, true);
	}

	private void execute(AspectAdviceRule aspectAdviceRule, boolean noThrow) {
		try {
			Executable action = aspectAdviceRule.getExecutableAction();
			
			if(action == null) {
				throw new IllegalArgumentException("No specified action on AspectAdviceRule " + aspectAdviceRule);
			}
			
			if(action.getActionType() == ActionType.BEAN && aspectAdviceRule.getAdviceBeanId() != null) {
				Object adviceBean = translet.getAspectAdviceBean(aspectAdviceRule.getAspectId());
				if(adviceBean == null) {
					if(aspectAdviceRule.getAdviceBeanClass() != null) {
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanClass());
					} else {
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
					}
					translet.putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
				}
			}
			
			Object adviceActionResult = action.execute(this);
			
			if(adviceActionResult != null && adviceActionResult != ActionResult.NO_RESULT) {
				translet.putAdviceResult(aspectAdviceRule, adviceActionResult);
			}
			
			if(log.isTraceEnabled()) {
				log.trace("adviceActionResult " + adviceActionResult);
			}
		} catch(Exception e) {
			if(aspectAdviceRule.getAspectRule().isIsolated()) {
				log.error("Failed to execute an isolated advice action " + aspectAdviceRule, e);
			} else {
				setRaisedException(e);
				if(noThrow) {
					log.error("Failed to execute an advice action " + aspectAdviceRule, e);
				} else {
					throw new ActionExecutionException("Failed to execute the advice action " + aspectAdviceRule, e);
				}
			}
		}
	}
	
	@Override
	public <T> T getAspectAdviceBean(String aspectId) {
		return translet.getAspectAdviceBean(aspectId);
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
	public MethodType getRequestMethod() {
		return requestMethod;
	}

	@Override
	public TransletRule getTransletRule() {
		return transletRule;
	}

	@Override
	public RequestRule getRequestRule() {
		return requestRule;
	}

	@Override
	public ResponseRule getResponseRule() {
		return responseRule;
	}

}
