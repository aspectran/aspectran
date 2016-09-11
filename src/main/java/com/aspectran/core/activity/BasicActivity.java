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

import com.aspectran.core.activity.aspect.AspectAdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class BasicActivity.
 *
 * <p>Created: 2016. 9. 10.</p>
 */
public abstract class BasicActivity extends AbstractActivity {

	private static final Log log = LogFactory.getLog(BasicActivity.class);

	/**
	 * Instantiates a new BasicActivity.
	 *
	 * @param context the activity context
	 */
	public BasicActivity(ActivityContext context) {
		super(context);
	}

	@Override
	public void exceptionHandling(List<ExceptionRule> exceptionRuleList) {
		for (ExceptionRule exceptionRule : exceptionRuleList) {
			exceptionHandling(exceptionRule);
			if (isResponseReserved()) {
				return;
			}
		}
	}

	protected void exceptionHandling(ExceptionRule exceptionRule) {
		if (log.isDebugEnabled()) {
			log.debug("Exception handling for raised exception: " + getRaisedException());
		}
		Executable action = exceptionRule.getExecutableAction();
		if (action != null) {
			execute(action);
		}
	}

	/**
	 * Executes an action.
	 *
	 * @param action the executable action
	 */
	private void execute(Executable action) {
		if (log.isDebugEnabled()) {
			log.debug("action " + action);
		}

		try {
			Object resultValue = action.execute(this);

			if (log.isTraceEnabled()) {
				log.trace("actionResult " + resultValue);
			}
		} catch (Exception e) {
			setRaisedException(e);
			throw new ActionExecutionException("Failed to execute action " + action, e);
		}
	}

	@Override
	public void execute(List<AspectAdviceRule> aspectAdviceRuleList) {
		for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
			execute(aspectAdviceRule, false);
		}
	}

	@Override
	public void executeWithoutThrow(List<AspectAdviceRule> aspectAdviceRuleList) {
		for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
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

	/**
	 * Execute advice action.
	 *
	 * @param aspectAdviceRule the aspect advice rule
	 * @param noThrow whether or not throw exception
	 */
	private void execute(AspectAdviceRule aspectAdviceRule, boolean noThrow) {
		try {
			Executable action = aspectAdviceRule.getExecutableAction();
			
			if (action == null) {
				throw new IllegalArgumentException("No specified action on AspectAdviceRule " + aspectAdviceRule);
			}
			
			if (action.getActionType() == ActionType.BEAN && aspectAdviceRule.getAdviceBeanId() != null) {
				Object adviceBean = getAspectAdviceBean(aspectAdviceRule.getAspectId());
				if (adviceBean == null) {
					if (aspectAdviceRule.getAdviceBeanClass() != null) {
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanClass());
					} else {
						adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
					}
					putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
				}
			}
			
			Object adviceActionResult = action.execute(this);
			
			if (adviceActionResult != null && adviceActionResult != ActionResult.NO_RESULT) {
				putAdviceResult(aspectAdviceRule, adviceActionResult);
			}
			
			if (log.isTraceEnabled()) {
				log.trace("adviceActionResult " + adviceActionResult);
			}
		} catch (Exception e) {
			if (aspectAdviceRule.getAspectRule().isIsolated()) {
				log.error("Failed to execute an isolated advice action " + aspectAdviceRule, e);
			} else {
				setRaisedException(e);
				if (noThrow) {
					log.error("Failed to execute an advice action " + aspectAdviceRule, e);
				} else {
					throw new AspectAdviceException("Failed to execute the advice action " + aspectAdviceRule, aspectAdviceRule, e);
				}
			}
		}
	}
	
}
