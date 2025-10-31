/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.activity.aspect.AdviceException;
import com.aspectran.core.activity.aspect.AdviceResult;
import com.aspectran.core.activity.process.action.ActionExecutionException;
import com.aspectran.core.activity.process.action.AnnotatedAdviceAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.component.aspect.AdviceRulePostRegister;
import com.aspectran.core.component.aspect.AdviceRuleRegistry;
import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract base class for activities that execute AOP (Aspect-Oriented Programming) advice.
 * <p>This class extends {@link AbstractActivity} and provides the core logic for managing and executing
 * various types of advice (e.g., Before, After, Finally, Thrown) as defined by {@link AspectRule}s.
 * It handles the registration of relevant aspect rules, the execution of advice actions, and the processing
 * of exceptions within the AOP context.</p>
 *
 * @since 2016. 9. 10.
 */
public abstract class AdviceActivity extends AbstractActivity {

    private static final Logger logger = LoggerFactory.getLogger(AdviceActivity.class);

    /** Registry for advice rules applicable to the current activity. */
    private AdviceRuleRegistry adviceRuleRegistry;

    /** Set of aspect rules that are relevant to the current execution context. */
    private Set<AspectRule> relevantAspectRules;

    /** Set of advice rules that have already been executed in the current phase. */
    private Set<AdviceRule> executedAdviceRules;

    /** The type of advice currently being processed (e.g., BEFORE, AFTER). */
    private AdviceType currentAdviceType;

    /** The specific advice rule currently being executed. */
    private AdviceRule currentAdviceRule;

    /** Stores the results of advice executions. */
    private AdviceResult adviceResult;

    /**
     * Instantiates a new AdviceActivity.
     * @param context the activity context
     */
    public AdviceActivity(ActivityContext context) {
        super(context);
    }

    /**
     * Retrieves a setting value, first from the superclass, then from any applicable {@link SettingsAdviceRule}s.
     * This allows for dynamic configuration overrides through aspects.
     * @param name the name of the setting
     * @param <V> the type of the setting value
     * @return the setting value, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getSetting(String name) {
        V value = super.getSetting(name);
        if (value != null) {
            return value;
        }
        if (adviceRuleRegistry != null && adviceRuleRegistry.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule settingsAdviceRule : adviceRuleRegistry.getSettingsAdviceRuleList()) {
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

    /**
     * Prepares the {@link AdviceRuleRegistry} for the current translet execution.
     * This method identifies and registers aspect rules that are relevant to the given translet
     * based on pointcut matching.
     * @param transletRule the translet rule being executed
     * @param requestName the name of the current request
     */
    protected void prepareAdviceRules(@NonNull TransletRule transletRule, String requestName) {
        AdviceRuleRegistry adviceRuleRegistryToUse;
        if (transletRule.hasPathVariables() || getActivityContext().getAspectRuleRegistry().hasNewAspectRules()) {
            AdviceRulePostRegister postRegister = new AdviceRulePostRegister();
            for (AspectRule aspectRule : getActivityContext().getAspectRuleRegistry().getAspectRules()) {
                if (!aspectRule.isBeanRelevant()) {
                    Pointcut pointcut = aspectRule.getPointcut();
                    if (pointcut == null || pointcut.matches(requestName)) {
                        postRegister.register(aspectRule);
                    }
                }
            }
            adviceRuleRegistryToUse = postRegister.getAdviceRuleRegistry();
        } else {
            adviceRuleRegistryToUse = transletRule.replicateAdviceRuleRegistry();
        }
        if (adviceRuleRegistryToUse != null) {
            if (this.adviceRuleRegistry != null) {
                this.adviceRuleRegistry.merge(adviceRuleRegistryToUse);
            } else {
                this.adviceRuleRegistry = adviceRuleRegistryToUse;
            }
        }
    }

    /**
     * Sets the current type of advice being processed (e.g., BEFORE, AFTER, FINALLY).
     * This is used for validation during advice registration.
     * @param adviceType the current advice type
     */
    protected void setCurrentAdviceType(AdviceType adviceType) {
        this.currentAdviceType = adviceType;
    }

    /**
     * Registers an {@link AspectRule} with this activity, making its advice applicable.
     * This method performs validation to ensure that advice is registered at an appropriate phase
     * of the activity lifecycle (e.g., BEFORE advice cannot be registered during the FINALLY phase).
     * @param aspectRule the aspect rule to register
     * @throws AdviceConstraintViolationException if the advice registration violates lifecycle constraints
     * @throws AdviceException if an error occurs during the execution of a newly registered advice
     */
    @Override
    public void registerAdviceRule(AspectRule aspectRule)
            throws AdviceConstraintViolationException, AdviceException {
        if (currentAdviceType == null) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at an UNKNOWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            if (msg != null) {
                logger.error(msg);
            }
            throw ex;
        }

        if (currentAdviceType == AdviceType.THROWN) {
            AdviceConstraintViolationException ex = new AdviceConstraintViolationException();
            String msg = "Advice can not be registered at the THROWN activity phase";
            msg = ex.addViolation(aspectRule, msg);
            if (msg != null) {
                logger.error(msg);
            }
            throw ex;
        }

        if (relevantAspectRules != null && relevantAspectRules.contains(aspectRule)) {
            return;
        }

        touchRelevantAspectRules().add(aspectRule);
        touchAdviceRuleRegistry().register(aspectRule);

        List<AdviceRule> adviceRuleList = aspectRule.getAdviceRuleList();
        if (adviceRuleList != null) {
            if (currentAdviceType == AdviceType.FINALLY) {
                // Exception thrown when registering BEFORE or AFTER advice in the FINALLY activity phase
                AdviceConstraintViolationException ex = null;
                for (AdviceRule adviceRule : adviceRuleList) {
                    AdviceType adviceType = adviceRule.getAdviceType();
                    if (adviceType == AdviceType.BEFORE || adviceType == AdviceType.AFTER) {
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
                AdviceRule adviceRule1 = currentAdviceRule;
                AdviceType adviceType1 = adviceRule1.getAdviceType();
                for (AdviceRule adviceRule2 : adviceRuleList) {
                    AdviceType adviceType2 = adviceRule2.getAdviceType();
                    if (adviceType1 == adviceType2) {
                        int order1 = adviceRule1.getAspectRule().getOrder();
                        int order2 = adviceRule2.getAspectRule().getOrder();
                        if (adviceType1 == AdviceType.BEFORE) {
                            if (order2 < order1) {
                                executeAdvice(adviceRule2);
                            }
                        } else {
                            if (order2 > order1) {
                                executeAdvice(adviceRule2);
                            }
                        }
                    } else if (adviceType2 == AdviceType.BEFORE) {
                        executeAdvice(adviceRule2);
                    }
                }
            } else {
                for (AdviceRule adviceRule : adviceRuleList) {
                    if (adviceRule.getAdviceType() == AdviceType.BEFORE) {
                        executeAdvice(adviceRule);
                    }
                }
            }
        }
    }

    /**
     * Registers a {@link SettingsAdviceRule} with this activity.
     * @param settingsAdviceRule the settings advice rule to register
     */
    @Override
    public void registerSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (relevantAspectRules != null && relevantAspectRules.contains(settingsAdviceRule.getAspectRule())) {
            return;
        }
        touchRelevantAspectRules().add(settingsAdviceRule.getAspectRule());
        touchAdviceRuleRegistry().addAdviceRule(settingsAdviceRule);
    }

    /**
     * Executes a list of {@link AdviceRule}s sequentially, ensuring each is executed only once.
     * @param adviceRuleList the list of advice rules to execute
     * @throws AdviceException if an error occurs during advice execution
     */
    @Override
    public void executeAdvice(List<AdviceRule> adviceRuleList) throws AdviceException {
        if (adviceRuleList != null && !adviceRuleList.isEmpty()) {
            while (true) {
                AdviceRule adviceRuleToUse = null;
                if (executedAdviceRules == null) {
                    adviceRuleToUse = adviceRuleList.getFirst();
                } else {
                    for (AdviceRule adviceRule : adviceRuleList) {
                        if (!executedAdviceRules.contains(adviceRule)) {
                            adviceRuleToUse = adviceRule;
                            break;
                        }
                    }
                }
                if (adviceRuleToUse != null) {
                    executeAdvice(adviceRuleToUse);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Executes a single {@link AdviceRule}.
     * This method handles the invocation of the advice action, manages advice results,
     * and propagates exceptions, respecting the 'isolated' property of the aspect rule.
     * @param adviceRule the advice rule to execute
     * @throws AdviceException if a non-isolated advice action fails
     */
    @Override
    public void executeAdvice(@NonNull AdviceRule adviceRule) throws AdviceException {
        if (adviceRule.getAspectRule().isDisabled() || !isAcceptable(adviceRule.getAspectRule())) {
            touchExecutedAdviceRules().add(adviceRule);
            return;
        }

        // for Finally thrown
        if (isExceptionRaised() && adviceRule.getExceptionRule() != null) {
            try {
                handleException(adviceRule.getExceptionRule());
            } catch (Exception e) {
                if (adviceRule.getAspectRule().isIsolated()) {
                    logger.error("Failed to execute isolated advice action {}", adviceRule, e);
                } else if (adviceRule.getAdviceType() == AdviceType.FINALLY) {
                    logger.error("Failed to execute advice action {}", adviceRule, e);
                } else {
                    throw new AdviceException("Failed to execute advice action " + adviceRule, adviceRule, e);
                }
            }
        }

        touchExecutedAdviceRules().add(adviceRule);

        Executable action = adviceRule.getAdviceAction();
        if (action != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Advice {}", AdviceRule.toString(action, adviceRule));
            }

            AdviceRule oldAdviceRule = currentAdviceRule;
            currentAdviceRule = adviceRule;
            try {
                Object adviceBean = getAdviceBean(adviceRule.getAspectId());
                if (adviceBean == null) {
                    resolveAdviceBean(adviceRule);
                }

                Object resultValue = action.execute(this);
                if (!action.isHidden() && resultValue != null && resultValue != Void.TYPE) {
                    putAdviceResult(adviceRule, resultValue);
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
                if (adviceRule.getAspectRule().isIsolated()) {
                    logger.error("Failed to execute isolated advice action {}", adviceRule, e);
                } else {
                    setRaisedException(e);
                    if (adviceRule.getAdviceType() == AdviceType.FINALLY) {
                        logger.error("Failed to execute advice action {}", adviceRule, e);
                    } else {
                        throw new AdviceException("Failed to execute advice action " + adviceRule, adviceRule, e);
                    }
                }
            } finally {
                currentAdviceRule = oldAdviceRule;
            }
        }
    }

    /**
     * Determines if an {@link AspectRule} is applicable to the current request by checking
     * its configured request methods and headers against the current request.
     * @param aspectRule the aspect rule to check
     * @return true if the aspect rule is acceptable, false otherwise
     */
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

    /**
     * Handles a list of {@link ExceptionRule}s, executing the appropriate exception handling advice.
     * @param exceptionRuleList the list of exception rules to process
     * @throws ActionExecutionException if an error occurs during the execution of an exception handling action
     */
    @Override
    public void handleException(List<ExceptionRule> exceptionRuleList) throws ActionExecutionException {
        if (exceptionRuleList != null) {
            for (ExceptionRule exceptionRule : exceptionRuleList) {
                handleException(exceptionRule);
            }
        }
    }

    /**
     * Handles a single {@link ExceptionRule}, executing the associated action if the current
     * raised exception matches the rule's criteria.
     * @param exceptionRule the exception rule to handle
     * @return the {@link ExceptionThrownRule} that was matched and handled, or {@code null} if no match
     * @throws ActionExecutionException if an error occurs during the execution of the exception handling action
     */
    protected ExceptionThrownRule handleException(ExceptionRule exceptionRule) throws ActionExecutionException {
        if (exceptionRule != null) {
            ExceptionThrownRule exceptionThrownRule = exceptionRule.getExceptionThrownRule(getRaisedException());
            if (exceptionThrownRule != null) {
                Executable action = exceptionThrownRule.getAction();
                if (action != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Advice {}", action);
                    }
                    try {
                        if (action.getActionType() == ActionType.INVOKE_ANNOTATED_ADVICE) {
                            AdviceRule adviceRule = ((AnnotatedAdviceAction)action).getAdviceRule();
                            Object adviceBean = getAdviceBean(adviceRule.getAspectId());
                            if (adviceBean == null) {
                                resolveAdviceBean(adviceRule);
                            }
                        }

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
                        throw new ActionExecutionException("Failed to execute exception handling advice action " +
                                action, e);
                    }
                }
                return exceptionThrownRule;
            }
        }
        return null;
    }

    /**
     * Returns the list of 'before' advice rules applicable to the current activity.
     * @return a list of before advice rules, or {@code null} if none
     */
    protected List<AdviceRule> getBeforeAdviceRuleList() {
        if (adviceRuleRegistry != null) {
            return adviceRuleRegistry.getBeforeAdviceRuleList();
        } else {
            return null;
        }
    }

    /**
     * Returns the list of 'after' advice rules applicable to the current activity.
     * @return a list of after advice rules, or {@code null} if none
     */
    protected List<AdviceRule> getAfterAdviceRuleList() {
        if (adviceRuleRegistry != null) {
            return adviceRuleRegistry.getAfterAdviceRuleList();
        } else {
            return null;
        }
    }

    /**
     * Returns the list of 'finally' advice rules applicable to the current activity.
     * @return a list of finally advice rules, or {@code null} if none
     */
    protected List<AdviceRule> getFinallyAdviceRuleList() {
        if (adviceRuleRegistry != null) {
            return adviceRuleRegistry.getFinallyAdviceRuleList();
        } else {
            return null;
        }
    }

    /**
     * Returns the list of exception rules applicable to the current activity.
     * @return a list of exception rules, or {@code null} if none
     */
    protected List<ExceptionRule> getExceptionRuleList() {
        if (adviceRuleRegistry != null) {
            return adviceRuleRegistry.getExceptionRuleList();
        } else {
            return null;
        }
    }

    /**
     * Retrieves a bean instance associated with a specific aspect ID.
     * This bean is typically the target object on which advice methods are invoked.
     * @param <V> the type of the advice bean
     * @param aspectId the ID of the aspect
     * @return the advice bean instance, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAdviceBean(String aspectId) {
        return (adviceResult != null ? (V) adviceResult.getAdviceBean(aspectId) : null);
    }

    /**
     * Stores an advice bean instance associated with a specific aspect ID.
     * @param aspectId the ID of the aspect
     * @param adviceBean the advice bean instance to store
     */
    private void putAdviceBean(String aspectId, Object adviceBean) {
        if (adviceResult == null) {
            adviceResult = new AdviceResult();
        }
        adviceResult.putAdviceBean(aspectId, adviceBean);
    }

    /**
     * Resolves and retrieves the advice bean for a given {@link AdviceRule}.
     * If the bean is not already cached, it is retrieved from the bean registry.
     * @param adviceRule the advice rule whose bean needs to be resolved
     */
    private void resolveAdviceBean(@NonNull AdviceRule adviceRule) {
        Object adviceBean = getAdviceBean(adviceRule.getAspectId());
        if (adviceBean == null) {
            if (adviceRule.getAdviceBeanClass() != null) {
                try {
                    adviceBean = getBean(adviceRule.getAdviceBeanClass());
                } catch (Exception e) {
                    logger.error("Failed to resolve advice bean {}", adviceRule, e);
                }
            } else if (adviceRule.getAdviceBeanId() != null) {
                try {
                    adviceBean = getBean(adviceRule.getAdviceBeanId());
                } catch (Exception e) {
                    logger.error("Failed to resolve advice bean {}", adviceRule, e);
                }
            }
            putAdviceBean(adviceRule.getAspectId(), adviceBean);
        }
    }

    /**
     * Retrieves the result of a 'before' advice execution for a given aspect ID.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the 'before' advice, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getBeforeAdviceResult(String aspectId) {
        return (adviceResult != null ? (V) adviceResult.getBeforeAdviceResult(aspectId) : null);
    }

    /**
     * Retrieves the result of an 'after' advice execution for a given aspect ID.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the 'after' advice, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAfterAdviceResult(String aspectId) {
        return (adviceResult != null ? (V) adviceResult.getAfterAdviceResult(aspectId) : null);
    }

    /**
     * Retrieves the result of an 'around' advice execution for a given aspect ID.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the 'around' advice, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAroundAdviceResult(String aspectId) {
        return (adviceResult != null ? (V) adviceResult.getAroundAdviceResult(aspectId) : null);
    }

    /**
     * Retrieves the result of a 'finally' advice execution for a given aspect ID.
     * @param <V> the type of the result
     * @param aspectId the ID of the aspect
     * @return the result of the 'finally' advice, or {@code null} if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getFinallyAdviceResult(String aspectId) {
        return (adviceResult != null ? (V) adviceResult.getFinallyAdviceResult(aspectId) : null);
    }

    /**
     * Stores the result of an advice action execution.
     * @param adviceRule the advice rule that was executed
     * @param adviceActionResult the result object from the advice action
     */
    protected void putAdviceResult(AdviceRule adviceRule, Object adviceActionResult) {
        if (adviceResult == null) {
            adviceResult = new AdviceResult();
        }
        adviceResult.putAdviceResult(adviceRule, adviceActionResult);
    }

    /**
     * Lazily initializes and returns the {@link AdviceRuleRegistry}.
     * @return the advice rule registry
     */
    private AdviceRuleRegistry touchAdviceRuleRegistry() {
        if (adviceRuleRegistry == null) {
            adviceRuleRegistry = new AdviceRuleRegistry();
        }
        return adviceRuleRegistry;
    }

    /**
     * Lazily initializes and returns the set of relevant aspect rules.
     * @return the set of relevant aspect rules
     */
    private Set<AspectRule> touchRelevantAspectRules() {
        if (relevantAspectRules == null) {
            relevantAspectRules = new HashSet<>();
        }
        return relevantAspectRules;
    }

    /**
     * Lazily initializes and returns the set of executed advice rules.
     * @return the set of executed advice rules
     */
    private Set<AdviceRule> touchExecutedAdviceRules() {
        if (executedAdviceRules == null) {
            executedAdviceRules = new HashSet<>();
        }
        return executedAdviceRules;
    }

}
