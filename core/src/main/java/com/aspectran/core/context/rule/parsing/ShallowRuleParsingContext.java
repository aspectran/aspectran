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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.appender.ShallowRuleAppendHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A lightweight, "shallow" implementation of {@link RuleParsingContext}.
 * <p>This version collects rule definitions without performing deep processing
 * such as class loading, bean reference validation, or alias resolution. It is
 * primarily used for tools or scenarios where a quick, non-validated overview
 * of the configuration rules is required.</p>
 *
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ShallowRuleParsingContext extends RuleParsingContext {

    private List<AspectRule> aspectRules;

    private List<BeanRule> beanRules;

    private List<ScheduleRule> scheduleRules;

    private List<TransletRule> transletRules;

    private List<TemplateRule> templateRules;

    /**
     * Constructs a new ShallowRuleParsingContext.
     */
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
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(aspectRule);
            return;
        }
        aspectRules.add(aspectRule);
    }

    @Override
    public void addBeanRule(BeanRule beanRule) {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(beanRule);
            return;
        }
        beanRules.add(beanRule);
    }

    @Override
    public void addInnerBeanRule(BeanRule beanRule) {
        // swallow
    }

    @Override
    public void addScheduleRule(ScheduleRule scheduleRule) {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(scheduleRule);
            return;
        }
        scheduleRules.add(scheduleRule);
    }

    @Override
    public void addTransletRule(TransletRule transletRule) {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(transletRule);
            return;
        }
        transletRules.add(transletRule);
    }

    @Override
    public void addTemplateRule(TemplateRule templateRule) {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(templateRule);
            return;
        }
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

}
