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

import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.appender.ShallowRuleAppendHandler;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class ShallowRuleParsingContext.
 *
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ShallowRuleParsingContext extends RuleParsingContext {

    private List<AspectRule> aspectRules;

    private List<BeanRule> beanRules;

    private List<ScheduleRule> scheduleRules;

    private List<TransletRule> transletRules;

    private List<TemplateRule> templateRules;

    public ShallowRuleParsingContext() {
        super();
    }

    @Override
    public void prepare() {
        super.prepare();

        aspectRules = new ArrayList<>();
        beanRules = new ArrayList<>();
        scheduleRules = new ArrayList<>();
        transletRules = new ArrayList<>();
        templateRules = new ArrayList<>();

        setRuleAppendHandler(new ShallowRuleAppendHandler(this));
    }

    @Override
    public void release() {
        super.release();

        aspectRules = null;
        beanRules = null;
        scheduleRules = null;
        transletRules = null;
        templateRules = null;
    }

    @Override
    public String resolveAliasType(String alias) {
        return alias;
    }

    @Override
    public String applyTransletNamePattern(String transletName) {
        return transletName;
    }

    @Override
    public void addAspectRule(AspectRule aspectRule) {
        aspectRules.add(aspectRule);
    }

    @Override
    public void addBeanRule(BeanRule beanRule) {
        beanRules.add(beanRule);
    }

    @Override
    public void addInnerBeanRule(BeanRule beanRule) {
        // swallow
    }

    @Override
    public void addScheduleRule(ScheduleRule scheduleRule) {
        scheduleRules.add(scheduleRule);
    }

    @Override
    public void addTransletRule(TransletRule transletRule) {
        transletRules.add(transletRule);
    }

    @Override
    public void addTemplateRule(TemplateRule templateRule) {
        templateRules.add(templateRule);
    }

    @Override
    public Collection<AspectRule> getAspectRules() {
        return aspectRules;
    }

    @Override
    public Collection<BeanRule> getBeanRules() {
        return beanRules;
    }

    @Override
    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRules;
    }

    @Override
    public Collection<TransletRule> getTransletRules() {
        return transletRules;
    }

    @Override
    public Collection<TemplateRule> getTemplateRules() {
        return templateRules;
    }

    @Override
    public void resolveBeanClass(BeanRule beanRule) {
        // Do Nothing
    }

    @Override
    public void resolveFactoryBeanClass(BeanRule beanRule) {
        // Do Nothing
    }

    @Override
    public void resolveAdviceBeanClass(@NonNull AspectRule aspectRule) {
        // Do Nothing
    }

    @Override
    public void resolveActionBeanClass(@NonNull InvokeActionRule invokeActionRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(@Nullable ItemRule itemRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(@Nullable Token[] tokens) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(Token token) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(@Nullable AutowireRule autowireRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(ScheduleRule scheduleRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(TemplateRule templateRule) {
        // Do Nothing
    }

    @Override
    public void reserveBeanReference(String beanId, Class<?> beanClass, BeanReferenceable referenceable) {
        // Do Nothing
    }

}
