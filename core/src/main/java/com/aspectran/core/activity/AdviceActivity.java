/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.activity.aspect.AdviceConstraintViolationException;
import com.aspectran.core.activity.aspect.AspectAdviceException;
import com.aspectran.core.activity.aspect.AspectAdviceResult;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract activity for executing advice injected between actions.
 *
 * <p>Created: 2016. 9. 10.</p>
 */
public abstract class AdviceActivity extends AbstractActivity {

    private static final Logger logger = LoggerFactory.getLogger(AdviceActivity.class);

    private Map<String, Object> settings;

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private Set<AspectRule> relevantAspectRules;

    private Set<AspectAdviceRule> executedAspectAdviceRules;

    private AspectAdviceType currentAspectAdviceType;

    private AspectAdviceRule currentAspectAdviceRule;

    private AspectAdviceResult aspectAdviceResult;

    /**
     * Instantiates a new AdviceActivity.
     * @param context the activity context
     */
    public AdviceActivity(ActivityContext context) {
        super(context);
    }

    protected void prepareAspectAdviceRule(TransletRule transletRule, String requestName) {
        AspectAdviceRuleRegistry aarr;
        if (transletRule.hasPathVariables()) {
            AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
            for (AspectRule aspectRule : getActivityContext().getAspectRuleRegistry().getAspectRules()) {
                if (!aspectRule.isBeanRelevant()) {
                    Pointcut pointcut = aspectRule.getPointcut();
                    if (pointcut == null || pointcut.matches(requestName)) {
                        postRegister.register(aspectRule);
                    }
                }
            }
            aarr = postRegister.getAspectAdviceRuleRegistry();
        } else {
            aarr = transletRule.replicateAspectAdviceRuleRegistry();
        }
        if (aarr != null) {
            if (this.aspectAdviceRuleRegistry != null) {
                this.aspectAdviceRuleRegistry.merge(aarr);
            } else {
                this.aspectAdviceRuleRegistry = aarr;
            }
        }
    }

    @Override
    public void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (relevantAspectRules != null && relevantAspectRules.contains(settingsAdviceRule.getAspectRule())) {
            return;
        }
        touchRelevantAspectRules().add(settingsAdviceRule.getAspectRule());
        touchAspectAdviceRuleRegistry().addAspectAdviceRule(settingsAdviceRule);
    }

    @Override
    public void registerAspectAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AspectAdviceException {
        if (currentAspectAdviceType == null) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at an UNKNOWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            logger.error(msg);
            throw ex;
        }

        if (currentAspectAdviceType == AspectAdviceType.THROWN) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at the THROWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            logger.error(msg);
            throw ex;
        }

        if (relevantAspectRules != null && relevantAspectRules.contains(aspectRule)) {
            return;
        }

        touchRelevantAspectRules().add(aspectRule);
        touchAspectAdviceRuleRegistry().register(aspectRule);

        List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
        if (aspectAdviceRuleList != null) {
            if (currentAspectAdviceType == AspectAdviceType.FINALLY) {
                // Exception thrown when registering BEFORE or AFTER advice at the FINALLY activity phase
                AdviceConstraintViolationException ex = null;
                for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                    AspectAdviceType aspectAdviceType = aspectAdviceRule.getAspectAdviceType();
                    if (aspectAdviceType == AspectAdviceType.BEFORE || aspectAdviceType == AspectAdviceType.AFTER) {
                        if (ex == null) {
                            ex = new AdviceConstraintViolationException();
                        }
                        String msg = "BEFORE or AFTER advice should never be registered after the FINALLY activity phase";
                        msg = ex.addViolation(aspectRule, msg);
                        if (msg != null) {
                            logger.error(msg);
                        }
                    }
                }
                if (ex != null) {
                    throw ex;
                }
            }
            if (currentAspectAdviceRule != null) {
                AspectAdviceRule adviceRule1 = currentAspectAdviceRule;
                AspectAdviceType adviceType1 = adviceRule1.getAspectAdviceType();
                for (AspectAdviceRule adviceRule2 : aspectAdviceRuleList) {
                    AspectAdviceType adviceType2 = adviceRule2.getAspectAdviceType();
                    if (adviceType1 == adviceType2) {
                        int order1 = adviceRule1.getAspectRule().getOrder();
                        int order2 = adviceRule2.getAspectRule().getOrder();
                        if (adviceType1 == AspectAdviceType.BEFORE) {
                            if (order2 < order1) {
                                executeAdvice(adviceRule2, true);
                            }
                        } else {
                            if (order2 > order1) {
                                executeAdvice(adviceRule2, true);
                            }
                        }
                    } else if (adviceType2 == AspectAdviceType.BEFORE) {
                        executeAdvice(adviceRule2, true);
                    }
                }
            } else {
                for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                    if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
                        executeAdvice(aspectAdviceRule, true);
                    }
                }
            }
        }
    }

    protected void setCurrentAspectAdviceType(AspectAdviceType aspectAdviceType) {
        this.currentAspectAdviceType = aspectAdviceType;
    }

    @Override
    public void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable)
            throws AspectAdviceException {
        if (aspectAdviceRuleList != null && !aspectAdviceRuleList.isEmpty()) {
            while (true) {
                AspectAdviceRule target = null;
                if (executedAspectAdviceRules == null) {
                    target = aspectAdviceRuleList.get(0);
                } else {
                    for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                        if (!executedAspectAdviceRules.contains(aspectAdviceRule)) {
                            target = aspectAdviceRule;
                            break;
                        }
                    }
                }
                if (target != null) {
                    executeAdvice(target, throwable);
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean throwable) throws AspectAdviceException {
        if (aspectAdviceRule.getAspectRule().isDisabled() || !isAcceptable(aspectAdviceRule.getAspectRule())) {
            touchExecutedAspectAdviceRules().add(aspectAdviceRule);
            return;
        }

        if (isExceptionRaised() && aspectAdviceRule.getExceptionRule() != null) {
            try {
                handleException(aspectAdviceRule.getExceptionRule());
            } catch (Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    logger.error("Failed to execute isolated advice action " + aspectAdviceRule, e);
                } else {
                    if (throwable) {
                        throw new AspectAdviceException("Failed to execute advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    } else {
                        logger.error("Failed to execute advice action " + aspectAdviceRule, e);
                    }
                }
            }
        }

        touchExecutedAspectAdviceRules().add(aspectAdviceRule);

        Executable action = aspectAdviceRule.getExecutableAction();
        if (action != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Advice " + action);
            }

            AspectAdviceRule oldAspectAdviceRule = currentAspectAdviceRule;
            currentAspectAdviceRule = aspectAdviceRule;
            try {
                if (action.getActionType() == ActionType.ACTION) {
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
                } else if (action.getActionType() == ActionType.ACTION_ANNOTATED) {
                    // If Annotated Aspect
                    Object adviceBean = getAspectAdviceBean(aspectAdviceRule.getAspectId());
                    if (adviceBean == null) {
                        adviceBean = getBean(aspectAdviceRule.getAdviceBeanClass());
                        putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
                    }
                }

                Object resultValue = action.execute(this);
                if (!action.isHidden() && resultValue != null && resultValue != ActionResult.NO_RESULT) {
                    putAdviceResult(aspectAdviceRule, resultValue);
                    if (action.getActionType() == ActionType.ECHO) {
                        if (action.getActionId() != null) {
                            getRequestAdapter().setAttribute(action.getActionId(), resultValue);
                        } else {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> echos = (Map<String, Object>)resultValue;
                            for (Map.Entry<String, Object> item : echos.entrySet()) {
                                getRequestAdapter().setAttribute(item.getKey(), item.getValue());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    logger.error("Failed to execute isolated advice action " + aspectAdviceRule, e);
                } else {
                    setRaisedException(e);
                    if (throwable) {
                        throw new AspectAdviceException("Failed to execute advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    } else {
                        logger.error("Failed to execute advice action " + aspectAdviceRule, e);
                    }
                }
            } finally {
                currentAspectAdviceRule = oldAspectAdviceRule;
            }
        }
    }

    private boolean isAcceptable(AspectRule aspectRule) {
        if (aspectRule.getMethods() != null) {
            if (getTranslet() == null) {
                return false;
            }
            MethodType requestMethod = getTranslet().getRequestMethod();
            if (requestMethod == null || !requestMethod.containsTo(aspectRule.getMethods())) {
                return false;
            }
        }
        if (aspectRule.getHeaders() != null) {
            for (String header : aspectRule.getHeaders()) {
                if (getRequestAdapter().containsHeader(header)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void handleException(List<ExceptionRule> exceptionRuleList) throws ActionExecutionException {
        if (exceptionRuleList != null) {
            for (ExceptionRule exceptionRule : exceptionRuleList) {
                handleException(exceptionRule);
            }
        }
    }

    protected ExceptionThrownRule handleException(ExceptionRule exceptionRule) throws ActionExecutionException {
        if (exceptionRule != null) {
            ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
            if (exceptionThrownRule != null) {
                Executable action = exceptionThrownRule.getAction();
                if (action != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Advice " + action);
                    }
                    try {
                        Object resultValue = action.execute(this);
                        if (getTranslet() != null && !action.isHidden() && resultValue != ActionResult.NO_RESULT) {
                            if (resultValue instanceof ProcessResult) {
                                getTranslet().setProcessResult((ProcessResult)resultValue);
                            } else {
                                ProcessResult processResult = getTranslet().getProcessResult();
                                ContentResult contentResult;
                                if (processResult == null) {
                                    processResult = new ProcessResult(1);
                                    contentResult = new ContentResult(processResult, 1);
                                    getTranslet().setProcessResult(processResult);
                                } else {
                                    contentResult = processResult.lastContentResult();
                                    if (contentResult == null) {
                                        contentResult = new ContentResult(processResult, 1);
                                    }
                                }
                                contentResult.addActionResult(action, resultValue);
                            }
                        }
                    } catch (Exception e) {
                        setRaisedException(e);
                        throw new ActionExecutionException("Failed to execute exception handling advice action " +
                                action, e);
                    }
                }
                return exceptionThrownRule;
            }
        }
        return null;
    }

    protected List<AspectAdviceRule> getBeforeAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<AspectAdviceRule> getAfterAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<AspectAdviceRule> getFinallyAdviceRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
        } else {
            return null;
        }
    }

    protected List<ExceptionRule> getExceptionRuleList() {
        if (aspectAdviceRuleRegistry != null) {
            return aspectAdviceRuleRegistry.getExceptionRuleList();
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getSetting(String name) {
        if (settings != null) {
            Object value = settings.get(name);
            if (value != null) {
                return (V)value;
            }
        }
        if (aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule settingsAdviceRule : aspectAdviceRuleRegistry.getSettingsAdviceRuleList()) {
                Object value = settingsAdviceRule.getSetting(name);
                if (value != null && isAcceptable(settingsAdviceRule.getAspectRule())) {
                    if (value instanceof String) {
                        return (V)TokenEvaluator.evaluate((String)value, this);
                    } else {
                        return (V)value;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void putSetting(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Setting name must not be null or empty");
        }
        if (settings == null) {
            settings = new LinkedHashMap<>();
        }
        settings.put(name, value);
    }

    /**
     * Gets the aspect advice bean.
     * @param <V> the type of the advice bean
     * @param aspectId the aspect id
     * @return the aspect advice bean
     */
    @SuppressWarnings("unchecked")
    public <V> V getAspectAdviceBean(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getAspectAdviceBean(aspectId) : null);
    }

    /**
     * Puts the aspect advice bean.
     * @param aspectId the aspect id
     * @param adviceBean the advice bean
     */
    protected void putAspectAdviceBean(String aspectId, Object adviceBean) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAspectAdviceBean(aspectId, adviceBean);
    }

    /**
     * Gets the before advice result.
     * @param <V> the result type of the before advice
     * @param aspectId the aspect id
     * @return the before advice result
     */
    @SuppressWarnings("unchecked")
    public <V> V getBeforeAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getBeforeAdviceResult(aspectId) : null);
    }

    /**
     * Gets the after advice result.
     * @param <V> the result type of the after advice
     * @param aspectId the aspect id
     * @return the after advice result
     */
    @SuppressWarnings("unchecked")
    public <V> V getAfterAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getAfterAdviceResult(aspectId) : null);
    }

    /**
     * Gets the around advice result.
     * @param <V> the result type of the around advice
     * @param aspectId the aspect id
     * @return the around advice result
     */
    @SuppressWarnings("unchecked")
    public <V> V getAroundAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getAroundAdviceResult(aspectId) : null);
    }

    /**
     * Gets the final advice result.
     * @param <V> the result type of the final advice
     * @param aspectId the aspect id
     * @return the result of the final advice
     */
    @SuppressWarnings("unchecked")
    public <V> V getFinallyAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getFinallyAdviceResult(aspectId) : null);
    }

    /**
     * Puts the result of the advice.
     * @param aspectAdviceRule the aspect advice rule
     * @param adviceActionResult the advice action result
     */
    protected void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
    }

    private AspectAdviceRuleRegistry touchAspectAdviceRuleRegistry() {
        if (aspectAdviceRuleRegistry == null) {
            aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
        }
        return aspectAdviceRuleRegistry;
    }

    private Set<AspectRule> touchRelevantAspectRules() {
        if (relevantAspectRules == null) {
            relevantAspectRules = new HashSet<>();
        }
        return relevantAspectRules;
    }

    private Set<AspectAdviceRule> touchExecutedAspectAdviceRules() {
        if (executedAspectAdviceRules == null) {
            executedAspectAdviceRules = new HashSet<>();
        }
        return executedAspectAdviceRules;
    }

}
