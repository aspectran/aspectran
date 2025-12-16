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
package com.aspectran.core.context.rule;

import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.component.aspect.pointcut.PointcutFactory;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.core.context.rule.params.JoinpointParameters;
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.AponReader;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an aspect that modularizes a cross-cutting concern.
 * This rule contains a {@link JoinpointRule} to specify where the aspect is applied
 * and a set of {@link AdviceRule}s that define the logic to be executed at those join points.
 *
 * <p>ex)
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint&gt;
 *     methods: [
 *       "GET"
 *       "POST"
 *       "PATCH"
 *       "PUT"
 *       "DELETE"
 *     ]
 *     headers: [
 *       "Origin"
 *     ]
 *     pointcut: {
 *       type: "wildcard"
 *       +: "/a/b@sample.bean1^method1"
 *       +: "/x/y@sample.bean2^method1"
 *       -: "/a/b/c@sample.bean3^method1"
 *       -: "/x/y/z@sample.bean4^method1"
 *     }
 *     pointcut: {
 *       type: "regexp"
 *       include: {
 *         translet: "/a/b"
 *         bean: "sample.bean1"
 *         method: "method1"
 *       }
 *       exclude: {
 *         translet: "/a/b/c"
 *         bean: "sample.bean3"
 *         method: "method1"
 *       }
 *     }
 *   &lt;/joinpoint&gt;
 *   &lt;settings&gt;
 *   &lt;/settings&gt;
 *   &lt;advice&gt;
 *   &lt;/advice&gt;
 *   &lt;exception&gt;
 *   &lt;/exception&gt;
 * &lt;aspect&gt;
 * </pre></p>
 */
public class AspectRule implements BeanReferenceable, Describable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.ASPECT_RULE;

    private String id;

    /**
     * The lowest value has the highest priority.
     * Normally starting with 0, with Integer.MAX_VALUE indicating the greatest value.
     * If it is the same value, the Aspect declared first takes precedence.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Even if an Advice execution error occurs, the entire flow is not stopped.
     * It is recommended to set true for aspects that are not related to business logic.
     */
    private Boolean isolated;

    private volatile Boolean disabled;

    private JoinpointRule joinpointRule;

    private Pointcut pointcut;

    private String adviceBeanId;

    private Class<?> adviceBeanClass;

    private SettingsAdviceRule settingsAdviceRule;

    private List<AdviceRule> adviceRuleList;

    private ExceptionRule exceptionRule;

    private boolean beanRelevant;

    private DescriptionRule descriptionRule;

    /**
     * Gets the aspect ID.
     * @return the aspect ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the aspect ID.
     * @param id the aspect ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the order of precedence for this aspect.
     * @return the order number
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order of precedence for this aspect.
     * @param order the order number
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Gets whether this aspect is isolated from the main flow.
     * @return true if isolated, false otherwise
     */
    public Boolean getIsolated() {
        return isolated;
    }

    /**
     * Returns whether this aspect is isolated from the main flow.
     * @return true if isolated, false otherwise
     */
    public boolean isIsolated() {
        return BooleanUtils.toBoolean(isolated);
    }

    /**
     * Sets whether this aspect is isolated from the main flow.
     * @param isolated true to isolate the aspect
     */
    public void setIsolated(Boolean isolated) {
        this.isolated = isolated;
    }

    /**
     * Gets whether this aspect is disabled.
     * @return true if disabled, false otherwise
     */
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * Returns whether this aspect is disabled.
     * @return true if disabled, false otherwise
     */
    public boolean isDisabled() {
        return BooleanUtils.toBoolean(disabled);
    }

    /**
     * Sets whether this aspect is disabled.
     * @param disabled true to disable the aspect
     */
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Gets the join point rule for this aspect.
     * @return the join point rule
     */
    public JoinpointRule getJoinpointRule() {
        return joinpointRule;
    }

    /**
     * Sets the join point rule and creates a corresponding {@link Pointcut}.
     * @param joinpointRule the join point rule
     */
    public void setJoinpointRule(JoinpointRule joinpointRule) {
        this.joinpointRule = joinpointRule;
        if (joinpointRule != null && joinpointRule.getPointcutRule() != null) {
            Pointcut pointcut = PointcutFactory.createPointcut(joinpointRule.getPointcutRule());
            setPointcut(pointcut);
        } else {
            setPointcut(null);
        }
    }

    /**
     * Gets the join point target type.
     * @return the join point target type
     */
    public JoinpointTargetType getJoinpointTargetType() {
        return (joinpointRule != null ? joinpointRule.getJoinpointTargetType() : null);
    }

    /**
     * Gets the allowed HTTP methods for the join point.
     * @return an array of allowed methods
     */
    public MethodType[] getMethods() {
        return (joinpointRule != null ? joinpointRule.getMethods() : null);
    }

    /**
     * Gets the required HTTP headers for the join point.
     * @return an array of required headers
     */
    public String[] getHeaders() {
        return (joinpointRule != null ? joinpointRule.getHeaders() : null);
    }

    /**
     * Gets the pointcut rule.
     * @return the pointcut rule
     */
    public PointcutRule getPointcutRule() {
        return (joinpointRule != null ? joinpointRule.getPointcutRule() : null);
    }

    /**
     * Gets the compiled pointcut expression.
     * @return the pointcut
     */
    public Pointcut getPointcut() {
        return pointcut;
    }

    private void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    /**
     * Gets the ID of the bean that contains the advice methods.
     * @return the advice bean ID
     */
    public String getAdviceBeanId() {
        return adviceBeanId;
    }

    /**
     * Sets the ID of the bean that contains the advice methods.
     * @param adviceBeanId the advice bean ID
     */
    public void setAdviceBeanId(String adviceBeanId) {
        this.adviceBeanId = adviceBeanId;
    }

    /**
     * Gets the class of the bean that contains the advice methods.
     * @return the advice bean class
     */
    public Class<?> getAdviceBeanClass() {
        return adviceBeanClass;
    }

    /**
     * Sets the class of the bean that contains the advice methods.
     * @param adviceBeanClass the advice bean class
     */
    public void setAdviceBeanClass(Class<?> adviceBeanClass) {
        this.adviceBeanClass = adviceBeanClass;
    }

    /**
     * Gets the settings advice rule.
     * @return the settings advice rule
     */
    public SettingsAdviceRule getSettingsAdviceRule() {
        return settingsAdviceRule;
    }

    /**
     * Sets the settings advice rule.
     * @param settingsAdviceRule the settings advice rule
     */
    public void setSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        this.settingsAdviceRule = settingsAdviceRule;
    }

    /**
     * Gets the settings advice rule, creating it if it does not exist.
     * @return the settings advice rule
     */
    public SettingsAdviceRule touchSettingsAdviceRule() {
        if (settingsAdviceRule == null) {
            settingsAdviceRule = new SettingsAdviceRule(this);
        }
        return settingsAdviceRule;
    }

    /**
     * Gets the list of advice rules (before, after, around, finally).
     * @return the list of advice rules
     */
    public List<AdviceRule> getAdviceRuleList() {
        return adviceRuleList;
    }

    /**
     * Sets the list of advice rules.
     * @param adviceRuleList the list of advice rules
     */
    public void setAdviceRuleList(List<AdviceRule> adviceRuleList) {
        this.adviceRuleList = adviceRuleList;
    }

    /**
     * Creates and adds a new advice rule of the specified type.
     * @param adviceType the type of advice
     * @return the new advice rule
     */
    public AdviceRule newAdviceRule(AdviceType adviceType) {
        AdviceRule adviceRule;
        adviceRule = new AdviceRule(this, adviceType);
        if (adviceType != AdviceType.THROWN) {
            touchAdviceRuleList().add(adviceRule);
        }
        return adviceRule;
    }

    /**
     * Creates and adds a new 'before' advice rule.
     * @return the new advice rule
     */
    public AdviceRule newBeforeAdviceRule() {
        return newAdviceRule(AdviceType.BEFORE);
    }

    /**
     * Creates and adds a new 'after' advice rule.
     * @return the new advice rule
     */
    public AdviceRule newAfterAdviceRule() {
        return newAdviceRule(AdviceType.AFTER);
    }

    /**
     * Creates and adds a new 'around' advice rule.
     * @return the new advice rule
     */
    public AdviceRule newAroundAdviceRule() {
        return newAdviceRule(AdviceType.AROUND);
    }

    /**
     * Creates and adds a new 'finally' advice rule.
     * @return the new advice rule
     */
    public AdviceRule newFinallyAdviceRule() {
        return newAdviceRule(AdviceType.FINALLY);
    }

    /**
     * Creates and adds a new 'thrown' advice rule.
     * @return the new advice rule
     */
    public AdviceRule newThrownAdviceRule() {
        return newAdviceRule(AdviceType.THROWN);
    }

    private List<AdviceRule> touchAdviceRuleList() {
        if (adviceRuleList == null) {
            adviceRuleList = new ArrayList<>();
        }
        return adviceRuleList;
    }

    /**
     * Gets the exception handling rule for this aspect.
     * @return the exception rule
     */
    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    /**
     * Sets the exception handling rule for this aspect.
     * @param exceptionRule the exception rule
     */
    public void setExceptionRule(ExceptionRule exceptionRule) {
        this.exceptionRule = exceptionRule;
    }

    /**
     * Adds a rule for a specific exception type to this aspect's exception handling.
     * @param exceptionThrownRule the rule for a specific exception
     */
    public void putExceptionThrownRule(ExceptionThrownRule exceptionThrownRule) {
        if (exceptionRule == null) {
            exceptionRule = new ExceptionRule();
        }
        exceptionRule.putExceptionThrownRule(exceptionThrownRule);
    }

    /**
     * Returns whether this aspect is relevant to bean method execution.
     * @return true if the aspect targets bean methods, false otherwise
     */
    public boolean isBeanRelevant() {
        return beanRelevant;
    }

    /**
     * Sets whether this aspect is relevant to bean method execution.
     * @param beanRelevant true if the aspect targets bean methods
     */
    public void setBeanRelevant(boolean beanRelevant) {
        this.beanRelevant = beanRelevant;
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        if (order != Integer.MAX_VALUE) {
            tsb.append("order", order);
        }
        tsb.append("isolated", isolated);
        tsb.append("disabled", disabled);
        tsb.append("joinpoint", joinpointRule);
        tsb.append("adviceBean", adviceBeanId);
        tsb.append("settings", settingsAdviceRule);
        tsb.append("advices", adviceRuleList);
        tsb.append("exception", exceptionRule);
        tsb.appendForce("beanRelevant", beanRelevant);
        return tsb.toString();
    }

    /**
     * Creates a new instance of AspectRule.
     * @param id the aspect ID
     * @param order the order of precedence
     * @param isolated whether the aspect is isolated
     * @param disabled whether the aspect is disabled
     * @return a new AspectRule instance
     * @throws IllegalRuleException if the ID is null
     */
    @NonNull
    public static AspectRule newInstance(String id, String order, Boolean isolated, Boolean disabled)
            throws IllegalRuleException {
        if (id == null) {
            throw new IllegalRuleException("The 'aspect' element requires an 'id' attribute");
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(id);
        aspectRule.setIsolated(isolated);
        aspectRule.setDisabled(disabled);

        if (StringUtils.hasLength(order)) {
            try {
                aspectRule.setOrder(Integer.parseInt(order));
            } catch (NumberFormatException e) {
                throw new IllegalRuleException("The value of 'order' attribute on element 'aspect' is not valid for 'integer'");
            }
        }

        return aspectRule;
    }

    /**
     * A static helper method to update the joinpoint configuration from an APON string.
     * @param aspectRule the aspect rule to update
     * @param target the joinpoint target type string
     * @param apon the APON string representing the joinpoint configuration
     * @throws IllegalRuleException if the APON string is invalid
     */
    public static void updateJoinpoint(@NonNull AspectRule aspectRule, String target, String apon)
            throws IllegalRuleException {
        JoinpointRule joinpointRule = JoinpointRule.newInstance();
        JoinpointRule.updateJoinpointTargetType(joinpointRule, target);
        JoinpointRule.updateJoinpoint(joinpointRule, apon);
        aspectRule.setJoinpointRule(joinpointRule);
    }

    /**
     * A static helper method to update the joinpoint configuration from a parameter map.
     * @param aspectRule the aspect rule to update
     * @param joinpointParameters the parameters representing the joinpoint configuration
     * @throws IllegalRuleException if the parameters are invalid
     */
    public static void updateJoinpoint(@NonNull AspectRule aspectRule, @NonNull JoinpointParameters joinpointParameters)
            throws IllegalRuleException {
        JoinpointRule joinpointRule = JoinpointRule.newInstance();

        String expression = joinpointParameters.getString(JoinpointParameters.expression);
        if (StringUtils.hasLength(expression)) {
            try {
                AponReader.read(expression, joinpointParameters);
                joinpointParameters.removeValue(JoinpointParameters.expression);
            } catch (AponParseException e) {
                // ignore
            }
        }

        JoinpointRule.updateJoinpoint(joinpointRule, joinpointParameters);
        aspectRule.setJoinpointRule(joinpointRule);
    }

}
