/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity related to advice.
 *
 * <p>Created: 2016. 9. 10.</p>
 */
public abstract class AdviceActivity extends AbstractActivity {

    private static final Log log = LogFactory.getLog(AdviceActivity.class);

    private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

    private Set<AspectRule> relevantAspectRules;

    private AspectAdviceType currentAspectAdviceType;

    private AspectAdviceRule currentAspectAdviceRule;

    private Set<AspectAdviceRule> executedAspectAdviceRules;

    private AspectAdviceResult aspectAdviceResult;

    /**
     * Instantiates a new AdviceActivity.
     *
     * @param context the activity context
     */
    public AdviceActivity(ActivityContext context) {
        super(context);
    }

    protected void prepareAspectAdviceRule(TransletRule transletRule, boolean merge) {
        AspectAdviceRuleRegistry aarr;
        if (transletRule.hasPathVariables()) {
            AspectAdviceRulePostRegister postRegister = new AspectAdviceRulePostRegister();
            for (AspectRule aspectRule : getActivityContext().getAspectRuleRegistry().getAspectRules()) {
                JoinpointTargetType joinpointTargetType = aspectRule.getJoinpointTargetType();
                if (!aspectRule.isBeanRelevant() && joinpointTargetType == JoinpointTargetType.TRANSLET) {
                    if (isAcceptable(aspectRule)) {
                        Pointcut pointcut = aspectRule.getPointcut();
                        if (pointcut == null || pointcut.matches(transletRule.getName())) {
                            if (log.isDebugEnabled()) {
                                log.debug("Aspect " + aspectRule);
                            }
                            postRegister.register(aspectRule);
                        }
                    }
                }
            }
            aarr = postRegister.getAspectAdviceRuleRegistry();
        } else {
            aarr = transletRule.replicateAspectAdviceRuleRegistry();
        }

        if (merge && aarr != null && this.aspectAdviceRuleRegistry != null) {
            this.aspectAdviceRuleRegistry.merge(aarr);
        } else {
            this.aspectAdviceRuleRegistry = aarr;
        }
    }

    @Override
    public void registerAspectRule(AspectRule aspectRule) {
        if (currentAspectAdviceType == null) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at an UNKNOWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            log.error(msg);
            throw ex;
        }

        if (currentAspectAdviceType == AspectAdviceType.THROWN) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at the THROWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            log.error(msg);
            throw ex;
        }

        if (relevantAspectRules != null && relevantAspectRules.contains(aspectRule)) {
            return;
        } else if (!isAcceptable(aspectRule)) {
            return;
        } else {
            touchRelevantAspectRules().add(aspectRule);
        }

        if (log.isDebugEnabled()) {
            log.debug("Aspect " + aspectRule);
        }

        List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
        if (aspectAdviceRuleList != null) {
            if (currentAspectAdviceType == AspectAdviceType.FINALLY ) {
                // Exception thrown when registering BEFORE or AFTER advice at the FINALLY activity phase
                AdviceConstraintViolationException ex = null;
                for (AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
                    AspectAdviceType aspectAdviceType = aspectAdviceRule.getAspectAdviceType();
                    if (aspectAdviceType == AspectAdviceType.BEFORE ||
                            aspectAdviceType == AspectAdviceType.AFTER) {
                        if (ex == null) {
                            ex = new AdviceConstraintViolationException();
                        }
                        String msg = "BEFORE or AFTER advice should never be registered after the FINALLY activity phase";
                        msg = ex.addViolation(aspectRule, msg);
                        if (msg != null) {
                            log.error(msg);
                        }
                    }
                }
                if (ex != null) {
                    throw ex;
                }
            }

            touchAspectAdviceRuleRegistry().register(aspectRule);

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

    @Override
    public void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (relevantAspectRules != null && relevantAspectRules.contains(settingsAdviceRule.getAspectRule())) {
            return;
        } else {
            touchRelevantAspectRules().add(settingsAdviceRule.getAspectRule());
        }
        touchAspectAdviceRuleRegistry().addAspectAdviceRule(settingsAdviceRule);
    }

    private boolean isAcceptable(AspectRule aspectRule) {
        if (aspectRule.getMethods() != null) {
            MethodType requestMethod = getTranslet().getRequestMethod();
            if (requestMethod == null || !requestMethod.containsTo(aspectRule.getMethods())) {
                return false;
            }
        }
        if (aspectRule.getHeaders() != null) {
            boolean contained = false;
            for (String header : aspectRule.getHeaders()) {
                if (getRequestAdapter().containsHeader(header)) {
                    contained = true;
                    break;
                }
            }
            return contained;
        }
        return true;
    }

    private Set<AspectRule> touchRelevantAspectRules() {
        if (relevantAspectRules == null) {
            relevantAspectRules = new HashSet<>();
        }
        return relevantAspectRules;
    }

    protected void setCurrentAspectAdviceType(AspectAdviceType aspectAdviceType) {
        this.currentAspectAdviceType = aspectAdviceType;
    }

    /**
     * Executes advice action.
     *
     * @param action the executable action
     */
    protected void executeAdvice(Executable action) {
        if (log.isDebugEnabled()) {
            log.debug("Action " + action);
        }

        try {
            action.execute(this);
        } catch (Exception e) {
            setRaisedException(e);
            throw new ActionExecutionException("Failed to execute advice action " + action, e);
        }
    }

    @Override
    public void executeAdvice(List<AspectAdviceRule> aspectAdviceRuleList, boolean throwable) {
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
    public void executeAdvice(AspectAdviceRule aspectAdviceRule, boolean throwable) {
        if (aspectAdviceRule.getAspectRule().isDisabled()) {
            touchExecutedAspectAdviceRules().add(aspectAdviceRule);
            return;
        }

        if (isExceptionRaised() && aspectAdviceRule.getExceptionRule() != null) {
            try {
                handleException(aspectAdviceRule.getExceptionRule());
            } catch (Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    log.error("Failed to execute isolated advice action " + aspectAdviceRule, e);
                } else {
                    if (throwable) {
                        throw new AspectAdviceException("Failed to execute advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    } else {
                        log.error("Failed to execute advice action " + aspectAdviceRule, e);
                    }
                }
            }
        }

        touchExecutedAspectAdviceRules().add(aspectAdviceRule);

        Executable action = aspectAdviceRule.getExecutableAction();
        if (action != null) {
            AspectAdviceRule oldAspectAdviceRule = currentAspectAdviceRule;
            currentAspectAdviceRule = aspectAdviceRule;
            try {
                if (action.getActionType() == ActionType.BEAN_METHOD) {
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
                } else if (action.getActionType() == ActionType.CONFIG_BEAN_METHOD) {
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
            } catch(Exception e) {
                if (aspectAdviceRule.getAspectRule().isIsolated()) {
                    log.error("Failed to execute an isolated advice action " + aspectAdviceRule, e);
                } else {
                    setRaisedException(e);
                    if (throwable) {
                        log.error("Failed to execute an advice action " + aspectAdviceRule, e);
                    } else {
                        throw new AspectAdviceException("Failed to execute an advice action " +
                                aspectAdviceRule, aspectAdviceRule, e);
                    }
                }
            } finally {
                currentAspectAdviceRule = oldAspectAdviceRule;
            }
        }
    }

    private Set<AspectAdviceRule> touchExecutedAspectAdviceRules() {
        if (executedAspectAdviceRules == null) {
            executedAspectAdviceRules = new HashSet<>();
        }
        return executedAspectAdviceRules;
    }

    @Override
    public void handleException(List<ExceptionRule> exceptionRuleList) {
        for (ExceptionRule exceptionRule : exceptionRuleList) {
            handleException(exceptionRule);
        }
    }

    protected void handleException(ExceptionRule exceptionRule) {
        ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
        if (exceptionThrownRule != null) {
            Executable action = exceptionThrownRule.getAction();
            if (action != null) {
                executeAdvice(action);
            }
        }
    }

    protected AspectAdviceRuleRegistry touchAspectAdviceRuleRegistry() {
        if (aspectAdviceRuleRegistry == null) {
            aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
        }
        return aspectAdviceRuleRegistry;
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
    public <T> T getSetting(String settingName) {
        if (aspectAdviceRuleRegistry != null) {
            Object value = aspectAdviceRuleRegistry.getSetting(settingName);
            if (value instanceof String) {
                value = TokenEvaluator.evaluate((String)value, this);
            }
            return (T)value;
        }
        return null;
    }

    /**
     * Gets the aspect advice bean.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the aspect advice bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getAspectAdviceBean(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAspectAdviceBean(aspectId) : null);
    }

    /**
     * Puts the aspect advice bean.
     *
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
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the before advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getBeforeAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getBeforeAdviceResult(aspectId) : null);
    }

    /**
     * Gets the after advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the after advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getAfterAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAfterAdviceResult(aspectId) : null);
    }

    /**
     * Gets the around advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the around advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getAroundAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getAroundAdviceResult(aspectId) : null);
    }

    /**
     * Gets the finally advice result.
     *
     * @param <T> the generic type
     * @param aspectId the aspect id
     * @return the finally advice result
     */
    @SuppressWarnings("unchecked")
    public <T> T getFinallyAdviceResult(String aspectId) {
        return (aspectAdviceResult != null ? (T)aspectAdviceResult.getFinallyAdviceResult(aspectId) : null);
    }

    /**
     * Puts the result of the advice.
     *
     * @param aspectAdviceRule the aspect advice rule
     * @param adviceActionResult the advice action result
     */
    protected void putAdviceResult(AspectAdviceRule aspectAdviceRule, Object adviceActionResult) {
        if (aspectAdviceResult == null) {
            aspectAdviceResult = new AspectAdviceResult();
        }
        aspectAdviceResult.putAdviceResult(aspectAdviceRule, adviceActionResult);
    }

}
