/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.component.aspect.AspectAdviceRulePostRegister;
import com.aspectran.core.component.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private Set<AspectRule> relevantAspectRules;

    private Set<AspectAdviceRule> executedAdviceRules;

    private AspectAdviceType currentAdviceType;

    private AspectAdviceRule currentAdviceRule;

    private AspectAdviceResult aspectAdviceResult;

    /**
     * Instantiates a new AdviceActivity.
     * @param context the activity context
     */
    public AdviceActivity(ActivityContext context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getSetting(String name) {
        V value = super.getSetting(name);
        if (value != null) {
            return value;
        }
        if (aspectAdviceRuleRegistry != null && aspectAdviceRuleRegistry.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule settingsAdviceRule : aspectAdviceRuleRegistry.getSettingsAdviceRuleList()) {
                value = settingsAdviceRule.getSetting(name);
                if (value != null && isAcceptable(settingsAdviceRule.getAspectRule())) {
                    if (value instanceof String str) {
                        return (V)TokenEvaluator.evaluate(str, this);
                    } else {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    protected void prepareAspectAdviceRules(@NonNull TransletRule transletRule, String requestName) {
        AspectAdviceRuleRegistry aarr;
        if (transletRule.hasPathVariables() || getActivityContext().getAspectRuleRegistry().hasNewAspectRules()) {
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

    protected void setCurrentAdviceType(AspectAdviceType aspectAdviceType) {
        this.currentAdviceType = aspectAdviceType;
    }

    @Override
    public void registerAspectAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AspectAdviceException {
        if (currentAdviceType == null) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at an UNKNOWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            logger.error(msg);
            throw ex;
        }

        if (currentAdviceType == AspectAdviceType.THROWN) {
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
            if (currentAdviceType == AspectAdviceType.FINALLY) {
                // Exception thrown when registering BEFORE or AFTER advice in the FINALLY activity phase
                AdviceConstraintViolationException ex = null;
                for (AspectAdviceRule adviceRule : aspectAdviceRuleList) {
                    AspectAdviceType adviceType = adviceRule.getAspectAdviceType();
                    if (adviceType == AspectAdviceType.BEFORE || adviceType == AspectAdviceType.AFTER) {
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
            if (currentAdviceRule != null) {
                AspectAdviceRule adviceRule1 = currentAdviceRule;
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

    @Override
    public void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (relevantAspectRules != null && relevantAspectRules.contains(settingsAdviceRule.getAspectRule())) {
            return;
        }
        touchRelevantAspectRules().add(settingsAdviceRule.getAspectRule());
        touchAspectAdviceRuleRegistry().addAspectAdviceRule(settingsAdviceRule);
    }

    @Override
    public void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable)
            throws AspectAdviceException {
        if (aspectAdviceRuleList != null && !aspectAdviceRuleList.isEmpty()) {
            while (true) {
                AspectAdviceRule target = null;
                if (executedAdviceRules == null) {
                    target = aspectAdviceRuleList.get(0);
                } else {
                    for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                        if (!executedAdviceRules.contains(aspectAdviceRule)) {
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
    public void executeAdvice(@NonNull AspectAdviceRule aspectAdviceRule, boolean throwable)
            throws AspectAdviceException {
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

        Executable action = aspectAdviceRule.getAdviceAction();
        if (action != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Advice " + AspectAdviceRule.toString(action, aspectAdviceRule));
            }

            AspectAdviceRule oldAdviceRule = currentAdviceRule;
            currentAdviceRule = aspectAdviceRule;
            try {
                Object adviceBean = getAspectAdviceBean(aspectAdviceRule.getAspectId());
                if (adviceBean == null) {
                    if (aspectAdviceRule.getAdviceBeanClass() != null) {
                        try {
                            adviceBean = getBean(aspectAdviceRule.getAdviceBeanClass());
                        } catch (Exception e) {
                            logger.error("Failed to load advice bean " + aspectAdviceRule, e);
                        }
                    } else if (aspectAdviceRule.getAdviceBeanId() != null) {
                        try {
                            adviceBean = getBean(aspectAdviceRule.getAdviceBeanId());
                        } catch (Exception e) {
                            logger.error("Failed to load advice bean " + aspectAdviceRule, e);
                        }
                    }
                    putAspectAdviceBean(aspectAdviceRule.getAspectId(), adviceBean);
                }

                Object resultValue = action.execute(this);
                if (!action.isHidden() && resultValue != null && resultValue != Void.TYPE) {
                    putAspectAdviceResult(aspectAdviceRule, resultValue);
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
                currentAdviceRule = oldAdviceRule;
            }
        }
    }

    private boolean isAcceptable(@NonNull AspectRule aspectRule) {
        if (aspectRule.getMethods() != null) {
            if (!hasTranslet()) {
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
                        if (hasTranslet() && !action.isHidden() && resultValue != Void.TYPE) {
                            if (resultValue instanceof ProcessResult processResult) {
                                getTranslet().setProcessResult(processResult);
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

    /**
     * Gets the aspect advice bean.
     * @param <V> the type of the advice bean
     * @param aspectId the aspect id
     * @return the aspect advice bean
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getFinallyAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (V)aspectAdviceResult.getFinallyAdviceResult(aspectId) : null);
    }

    /**
     * Puts the result of the advice.
     * @param aspectAdviceRule the aspect advice rule
     * @param adviceActionResult the advice action result
     */
    protected void putAspectAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAspectAdviceResult(aspectAdviceRule, adviceActionResult);
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
        if (executedAdviceRules == null) {
            executedAdviceRules = new HashSet<>();
        }
        return executedAdviceRules;
    }

}
