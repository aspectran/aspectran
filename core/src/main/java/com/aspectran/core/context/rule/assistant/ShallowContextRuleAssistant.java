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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.appender.ShallowRuleAppendHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class ShallowContextRuleAssistant.
 * 
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ShallowContextRuleAssistant extends ContextRuleAssistant {

    private List<AspectRule> aspectRules;

    private List<BeanRule> beanRules;

    private List<ScheduleRule> scheduleRules;

    private List<TransletRule> transletRules;

    private List<TemplateRule> templateRules;

    public ShallowContextRuleAssistant() {
        super();
    }

    @Override
    public void ready() {
        super.ready();

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
    public void applyTransletInterface(DefaultSettings defaultSettings) throws ClassNotFoundException {
        // Do Nothing
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
    public void resolveAdviceBeanClass(AspectRule aspectRule) {
        // Do Nothing
    }

    @Override
    public void resolveActionBeanClass(BeanActionRule beanActionRule) {
        // Do Nothing
    }

    @Override
    public void resolveFactoryBeanClass(BeanRule beanRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(ItemRule itemRule) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(Token[] tokens) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(Token token) {
        // Do Nothing
    }

    @Override
    public void resolveBeanClass(AutowireRule autowireRule) {
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
    public void reserveBeanReference(String beanId, Class<?> beanClass, BeanReferenceInspectable inspectable) {
        // Do Nothing
    }

}
