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
package com.aspectran.core.context.rule.parsing;

import com.aspectran.core.context.asel.bean.ValueProvider;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.value.ValueExpression;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

/**
 * Resolves Java class objects for various configuration rules during parsing.
 */
public class BeanClassResolver {

    private final RuleParsingContext ruleParsingContext;

    /**
     * Constructs a new BeanClassResolver.
     * @param ruleParsingContext the rule parsing context
     */
    public BeanClassResolver(@NonNull RuleParsingContext ruleParsingContext) {
        Assert.notNull(ruleParsingContext, "ruleParsingContext must not be null");
        this.ruleParsingContext = ruleParsingContext;
    }

    private BeanReferenceInspector getBeanReferenceInspector() {
        return ruleParsingContext.getBeanReferenceInspector();
    }

    @Nullable
    private RuleAppender getCurrentRuleAppender() {
        return (ruleParsingContext.getRuleAppendHandler() != null ?
                ruleParsingContext.getRuleAppendHandler().getCurrentRuleAppender() : null);
    }

    private void reserveBeanReference(String beanId, BeanReferenceable referenceable) {
        BeanReferenceInspector inspector = getBeanReferenceInspector();
        if (inspector != null) {
            inspector.reserve(beanId, referenceable, getCurrentRuleAppender());
        }
    }

    private void reserveBeanReference(Class<?> beanClass, BeanReferenceable referenceable) {
        BeanReferenceInspector inspector = getBeanReferenceInspector();
        if (inspector != null) {
            inspector.reserve(beanClass, referenceable, getCurrentRuleAppender());
        }
    }

    private void reserveBeanReference(String beanId, Class<?> beanClass, BeanReferenceable referenceable) {
        BeanReferenceInspector inspector = getBeanReferenceInspector();
        if (inspector != null) {
            inspector.reserve(beanId, beanClass, referenceable, getCurrentRuleAppender());
        }
    }

    /**
     * Resolves the Java class for a specified bean rule.
     * @param beanRule the bean rule
     * @throws IllegalRuleException if the class cannot be loaded
     */
    public void resolveBeanClass(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule != null && !beanRule.isFactoryOffered() && beanRule.getClassName() != null) {
            Class<?> beanClass = loadClass(beanRule.getClassName(), beanRule);
            beanRule.setBeanClass(beanClass);
        }
    }

    /**
     * Resolves the factory bean class or reserves a bean reference for a factory bean rule.
     * @param beanRule the bean rule
     * @throws IllegalRuleException if the class cannot be loaded
     */
    public void resolveFactoryBeanClass(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule != null && beanRule.isFactoryOffered() && beanRule.getFactoryBeanId() != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanRule.getFactoryBeanId(), beanRule);
            if (beanClass != null) {
                beanRule.setFactoryBeanClass(beanClass);
                reserveBeanReference(beanClass, beanRule);
            } else {
                reserveBeanReference(beanRule.getFactoryBeanId(), beanRule);
            }
        }
    }

    /**
     * Resolves the advice bean class or reserves a bean reference for an aspect rule.
     * @param aspectRule the aspect rule
     * @throws IllegalRuleException if the class cannot be loaded
     */
    public void resolveAdviceBeanClass(@NonNull AspectRule aspectRule) throws IllegalRuleException {
        String beanIdOrClass = aspectRule.getAdviceBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanIdOrClass, aspectRule);
            if (beanClass != null) {
                aspectRule.setAdviceBeanClass(beanClass);
                reserveBeanReference(beanClass, aspectRule);
            } else {
                reserveBeanReference(beanIdOrClass, aspectRule);
            }
        }
    }

    /**
     * Resolves the bean class or reserves a bean reference for an invoke action rule.
     * @param invokeActionRule the invoke action rule
     * @throws IllegalRuleException if the class cannot be loaded
     */
    public void resolveActionBeanClass(@NonNull InvokeActionRule invokeActionRule) throws IllegalRuleException {
        String beanIdOrClass = invokeActionRule.getBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanIdOrClass, invokeActionRule);
            if (beanClass != null) {
                invokeActionRule.setBeanClass(beanClass);
                reserveBeanReference(beanClass, invokeActionRule);
            } else {
                reserveBeanReference(beanIdOrClass, invokeActionRule);
            }
        }
    }

    /**
     * Resolves bean classes embedded within an item rule.
     * @param itemRule the item rule
     * @throws IllegalRuleException if any embedded class cannot be loaded
     */
    public void resolveBeanClass(@Nullable ItemRule itemRule) throws IllegalRuleException {
        if (itemRule != null) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                if (itemRule.isListableType()) {
                    if (itemRule.getBeanRuleList() != null) {
                        for (BeanRule beanRule : itemRule.getBeanRuleList()) {
                            resolveBeanClass(beanRule);
                        }
                    }
                } else if (itemRule.isMappableType()) {
                    if (itemRule.getBeanRuleMap() != null) {
                        for (BeanRule beanRule : itemRule.getBeanRuleMap().values()) {
                            resolveBeanClass(beanRule);
                        }
                    }
                } else {
                    resolveBeanClass(itemRule.getBeanRule());
                }
            } else {
                Iterator<Token[]> it = ItemRuleUtils.tokenIterator(itemRule);
                if (it != null) {
                    while (it.hasNext()) {
                        Token[] tokens = it.next();
                        if (tokens != null) {
                            for (Token token : tokens) {
                                resolveBeanClass(token);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves bean classes for an array of tokens.
     * @param tokens an array of tokens
     * @throws IllegalRuleException if any token's bean class cannot be loaded
     */
    public void resolveBeanClass(@Nullable Token[] tokens) throws IllegalRuleException {
        if (tokens != null) {
            for (Token token : tokens) {
                resolveBeanClass(token);
            }
        }
    }

    /**
     * Resolves the bean class for a specified token.
     * @param token the token
     * @throws IllegalRuleException if the token's bean class cannot be loaded
     */
    public void resolveBeanClass(Token token) throws IllegalRuleException {
        resolveBeanClass(token, token);
    }

    private void resolveBeanClass(@Nullable Token token, @Nullable BeanReferenceable referenceable)
            throws IllegalRuleException {
        if (token != null && token.getType() == TokenType.BEAN) {
            try {
                Token.resolveValueProvider(token, ruleParsingContext.getClassLoader());
            } catch (RuntimeException e) {
                throw new IllegalRuleException("Failed to resolve value provider for token: " + token, e);
            }

            ValueProvider provider = token.getValueProvider();
            if (provider != null) {
                if (provider.isRequiresBeanInstance()) {
                    reserveBeanReference(provider.getDependentBeanType(), referenceable);
                }
            } else if (token.getDirectiveType() == null) {
                // This is for simple #{beanId} tokens that don't have a provider
                reserveBeanReference(token.getName(), referenceable);
            }
        }
    }

    private void resolveBeanClass(Token[] tokens, BeanReferenceable referenceable) throws IllegalRuleException {
        if (tokens != null) {
            for (Token token : tokens) {
                resolveBeanClass(token, referenceable);
            }
        }
    }

    /**
     * Resolves target bean classes or reserves references for an autowire rule.
     * @param autowireRule the autowire rule
     * @throws IllegalRuleException if any target class cannot be loaded
     */
    public void resolveBeanClass(@Nullable AutowireRule autowireRule) throws IllegalRuleException {
        if (autowireRule != null) {
            if (autowireRule.getTargetType() == AutowireTargetType.FIELD) {
                AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                if (autowireRule.isRequired() && autowireTargetRule != null && !autowireTargetRule.isInnerBean()) {
                    resolveBeanClassOrReference(autowireRule, autowireTargetRule, true);
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                if (autowireRule.isRequired() && autowireTargetRule != null && !autowireTargetRule.isInnerBean()) {
                    resolveBeanClassOrReference(autowireRule, autowireTargetRule, false);
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD ||
                autowireRule.getTargetType() == AutowireTargetType.CONSTRUCTOR) {
                AutowireTargetRule[] autowireTargetRules = autowireRule.getAutowireTargetRules();
                if (autowireTargetRules != null && autowireRule.isRequired()) {
                    for (AutowireTargetRule autowireTargetRule : autowireTargetRules) {
                        if (!autowireTargetRule.isOptional() && !autowireTargetRule.isInnerBean()) {
                            resolveBeanClassOrReference(autowireRule, autowireTargetRule, true);
                        }
                    }
                }
            }
        }
    }

    private void resolveBeanClassOrReference(
            AutowireRule autowireRule, @NonNull AutowireTargetRule autowireTargetRule, boolean forReference)
            throws IllegalRuleException {
        ValueExpression valueExpression = autowireTargetRule.getValueExpression();
        if (valueExpression != null) {
            Token[] tokens = valueExpression.getTokens();
            resolveBeanClass(tokens, autowireRule);
        } else if (forReference) {
            Class<?> type = autowireTargetRule.getType();
            String qualifier = autowireTargetRule.getQualifier();
            reserveBeanReference(qualifier, type, autowireRule);
        }
    }

    /**
     * Resolves the scheduler bean class or reserves a bean reference for a schedule rule.
     * @param scheduleRule the schedule rule
     * @throws IllegalRuleException if the scheduler class cannot be loaded
     */
    public void resolveBeanClass(ScheduleRule scheduleRule) throws IllegalRuleException {
        if (scheduleRule != null) {
            String beanId = scheduleRule.getSchedulerBeanId();
            if (beanId != null) {
                Class<?> beanClass = resolveDirectiveBeanClass(beanId, scheduleRule);
                if (beanClass != null) {
                    scheduleRule.setSchedulerBeanClass(beanClass);
                    reserveBeanReference(beanClass, scheduleRule);
                } else {
                    reserveBeanReference(beanId, scheduleRule);
                }
            }
        }
    }

    /**
     * Resolves the engine bean class or template tokens for a template rule.
     * @param templateRule the template rule
     * @throws IllegalRuleException if the engine class cannot be loaded
     */
    public void resolveBeanClass(TemplateRule templateRule) throws IllegalRuleException {
        if (templateRule != null) {
            String beanId = templateRule.getEngineBeanId();
            if (beanId != null) {
                Class<?> beanClass = resolveDirectiveBeanClass(beanId, templateRule);
                if (beanClass != null) {
                    templateRule.setEngineBeanClass(beanClass);
                    reserveBeanReference(beanClass, templateRule);
                } else {
                    reserveBeanReference(beanId, templateRule);
                }
            } else {
                resolveBeanClass(templateRule.getTemplateTokens());
            }
        }
    }

    private Class<?> resolveDirectiveBeanClass(String beanIdOrClass, Object referer) throws IllegalRuleException {
        if (beanIdOrClass != null && beanIdOrClass.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = beanIdOrClass.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            return loadClass(className, referer);
        } else {
            return null;
        }
    }

    private Class<?> loadClass(String className, Object referer) throws IllegalRuleException {
        try {
            return ruleParsingContext.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalRuleException("Unable to load class " + className + " for " + referer, e);
        }
    }

}
