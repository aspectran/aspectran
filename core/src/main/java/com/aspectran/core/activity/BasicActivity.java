/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.activity.aspect.AspectAdviceException;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.List;
import java.util.Map;

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

    /**
     * Executes advice action.
     *
     * @param action the executable action
     */
    protected void executeAdvice(Executable action) {
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
            throw new ActionExecutionException("Failed to execute advice action " + action, e);
        }
    }

    @Override
    public void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList) {
        for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
            executeAdvice(aspectAdviceRule, false);
        }
    }

    @Override
    public void executeAdviceWithoutThrow(List<AspectAdviceRule> aspectAdviceRuleList) {
        for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
            executeAdvice(aspectAdviceRule, true);
        }
    }

    @Override
    public void executeAdvice(AspectAdviceRule aspectAdviceRule) {
        executeAdvice(aspectAdviceRule, false);
    }

    @Override
    public void executeAdviceWithoutThrow(AspectAdviceRule aspectAdviceRule) {
        executeAdvice(aspectAdviceRule, true);
    }

    /**
     * Executes advice action.
     *
     * @param aspectAdviceRule the aspect advice rule
     * @param noThrow whether or not throw exception
     */
    private void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean noThrow) {
        if (isExceptionRaised() && aspectAdviceRule.getExceptionRule() != null) {
            try {
                handleException(aspectAdviceRule.getExceptionRule());
            } catch (Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    log.error("Failed to execute isolated advice action " + aspectAdviceRule, e);
                } else {
                    if (noThrow) {
                        log.error("Failed to execute advice action " + aspectAdviceRule, e);
                    } else {
                        throw new AspectAdviceException("Failed to execute advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    }
                }
            }
        }

        Executable action = aspectAdviceRule.getExecutableAction();
        if (action != null) {
            try {
                if (action.getActionType() == ActionType.BEAN) {
                    // If Aspect Advice Bean ID is specified
                    if (aspectAdviceRule.getAdviceBeanId() != null) {
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
                } else if (action.getActionType() == ActionType.METHOD) {
                    // If Annotated Aspect
                    Object adviceBean = getAspectAdviceBean(aspectAdviceRule.getAspectId());
                    if (adviceBean == null) {
                        adviceBean = getConfigBean(aspectAdviceRule.getAdviceBeanClass());
                        putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
                    }
                }

                Object result = action.execute(this);
                if (result != null && result != ActionResult.NO_RESULT) {
                    putAdviceResult(aspectAdviceRule, result);
                    if (action.getActionType() == ActionType.ECHO) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> echos = (Map<String, Object>)result;
                        for (Map.Entry<String, Object> item : echos.entrySet()) {
                            getRequestAdapter().setAttribute(item.getKey(), item.getValue());
                        }
                    }
                }

                if (log.isTraceEnabled()) {
                    log.trace("adviceActionResult " + result);
                }
            } catch(Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    log.error("Failed to execute an isolated advice action " + aspectAdviceRule, e);
                } else {
                    setRaisedException(e);
                    if (noThrow) {
                        log.error("Failed to execute an advice action " + aspectAdviceRule, e);
                    } else {
                        throw new AspectAdviceException("Failed to execute an advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    }
                }
            }
        }
    }

    @Override
    public void handleException(List<ExceptionRule> exceptionRuleList) {
        for (ExceptionRule exceptionRule : exceptionRuleList) {
            handleException(exceptionRule);
        }
    }

    protected void handleException(ExceptionRule exceptionRule) {
        if (log.isDebugEnabled()) {
            log.debug("Handling the Exception Raised: " + getRootCauseOfRaisedException());
        }

        ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
        if (exceptionThrownRule != null) {
            Executable action = exceptionThrownRule.getExecutableAction();
            if (action != null) {
                executeAdvice(action);
            }
        }
    }

}
