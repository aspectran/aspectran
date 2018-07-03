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
package com.aspectran.core.context.rule;

import com.aspectran.core.component.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.params.JoinpointParameters;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.AponParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AspectRule.
 * 
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint target="translet"&gt;
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
 * </pre>
 */
public class AspectRule implements BeanReferenceInspectable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.ASPECT_RULE;

    private String id;

    /**
     * The lowest value has highest priority.
     * Normally starting with 0, with Integer.MAX_VALUE indicating the greatest value.
     * If it is the same value, the Aspect declared first takes precedence.
     */
    private int order = Integer.MAX_VALUE;

    private Boolean isolated;

    private JoinpointRule joinpointRule;

    private Pointcut pointcut;

    private String adviceBeanId;

    private Class<?> adviceBeanClass;

    private SettingsAdviceRule settingsAdviceRule;

    private List<AspectAdviceRule> aspectAdviceRuleList;

    private ExceptionRule exceptionRule;

    private boolean beanRelevant;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Boolean getIsolated() {
        return isolated;
    }

    public boolean isIsolated() {
        return BooleanUtils.toBoolean(isolated, false);
    }

    public void setIsolated(Boolean isolated) {
        this.isolated = isolated;
    }

    public JoinpointRule getJoinpointRule() {
        return joinpointRule;
    }

    public void setJoinpointRule(JoinpointRule joinpointRule) {
        this.joinpointRule = joinpointRule;
    }

    public JoinpointTargetType getJoinpointTargetType() {
        return (joinpointRule != null ? joinpointRule.getJoinpointTargetType() : null);
    }

    public MethodType[] getMethods() {
        return (joinpointRule != null ? joinpointRule.getMethods() : null);
    }

    public String[] getHeaders() {
        return (joinpointRule != null ? joinpointRule.getHeaders() : null);
    }

    public PointcutRule getPointcutRule() {
        return (joinpointRule != null ? joinpointRule.getPointcutRule() : null);
    }

    public Pointcut getPointcut() {
        return pointcut;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    public String getAdviceBeanId() {
        return adviceBeanId;
    }

    public void setAdviceBeanId(String adviceBeanId) {
        this.adviceBeanId = adviceBeanId;
    }

    public Class<?> getAdviceBeanClass() {
        return adviceBeanClass;
    }

    public void setAdviceBeanClass(Class<?> adviceBeanClass) {
        this.adviceBeanClass = adviceBeanClass;
    }

    public SettingsAdviceRule getSettingsAdviceRule() {
        return settingsAdviceRule;
    }

    public void setSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        this.settingsAdviceRule = settingsAdviceRule;
    }

    public SettingsAdviceRule touchSettingsAdviceRule() {
        if (settingsAdviceRule == null) {
            settingsAdviceRule = new SettingsAdviceRule(this);
        }
        return settingsAdviceRule;
    }

    public List<AspectAdviceRule> getAspectAdviceRuleList() {
        return aspectAdviceRuleList;
    }

    public void setAspectAdviceRuleList(List<AspectAdviceRule> aspectAdviceRuleList) {
        this.aspectAdviceRuleList = aspectAdviceRuleList;
    }

    public AspectAdviceRule touchAspectAdviceRule(AspectAdviceType aspectAdviceType) {
        if (aspectAdviceType == null) {
            throw new IllegalArgumentException("Argument 'aspectAdviceType' must not be null");
        }
        AspectAdviceRule aspectAdviceRule = new AspectAdviceRule(this, aspectAdviceType);
        touchAspectAdviceRuleList().add(aspectAdviceRule);
        return aspectAdviceRule;
    }

    private List<AspectAdviceRule> touchAspectAdviceRuleList() {
        if (aspectAdviceRuleList == null) {
            aspectAdviceRuleList = new ArrayList<>();
        }
        return aspectAdviceRuleList;
    }

    public ExceptionRule getExceptionRule() {
        return exceptionRule;
    }

    public void setExceptionRule(ExceptionRule exceptionRule) {
        this.exceptionRule = exceptionRule;
    }

    public void putExceptionThrownRule(ExceptionThrownRule exceptionThrownRule) {
        if (exceptionRule == null) {
            exceptionRule = new ExceptionRule();
        }
        exceptionRule.putExceptionThrownRule(exceptionThrownRule);
    }

    public boolean isBeanRelevant() {
        return beanRelevant;
    }

    public void setBeanRelevant(boolean beanRelevant) {
        this.beanRelevant = beanRelevant;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
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
        tsb.append("joinpoint", joinpointRule);
        tsb.append("adviceBean", adviceBeanId);
        tsb.append("settingsAdvice", settingsAdviceRule);
        tsb.append("aspectAdvices", aspectAdviceRuleList);
        tsb.append("exception", exceptionRule);
        tsb.append("beanRelevant", beanRelevant);
        return tsb.toString();
    }

    public static AspectRule newInstance(String id, String order, Boolean isolated) throws IllegalRuleException {
        if (id == null) {
            throw new IllegalRuleException("The 'aspect' element requires an 'id' attribute");
        }

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(id);
        aspectRule.setIsolated(isolated);

        if (!StringUtils.isEmpty(order)) {
            try {
                aspectRule.setOrder(Integer.parseInt(order));
            } catch (NumberFormatException e) {
                throw new IllegalRuleException("The 'order' attribute of the 'aspect' element is not a valid integer");
            }
        }

        return aspectRule;
    }

    public static void updateJoinpoint(AspectRule aspectRule, String type, String text)
            throws IllegalRuleException, AponParseException {
        JoinpointRule joinpointRule = JoinpointRule.newInstance();
        JoinpointRule.updateJoinpointTargetType(joinpointRule, type);
        JoinpointRule.updateJoinpoint(joinpointRule, text);
        aspectRule.setJoinpointRule(joinpointRule);
    }

    public static void updateJoinpoint(AspectRule aspectRule, JoinpointParameters joinpointParameters)
            throws IllegalRuleException {
        JoinpointRule joinpointRule = JoinpointRule.newInstance();
        JoinpointRule.updateJoinpoint(joinpointRule, joinpointParameters);
        aspectRule.setJoinpointRule(joinpointRule);
    }

}
