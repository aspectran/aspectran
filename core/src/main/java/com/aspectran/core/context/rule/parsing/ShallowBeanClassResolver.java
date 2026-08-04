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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A no-op implementation of {@link BeanClassResolver} used during shallow rule parsing.
 * <p>Prevents class loading, token value provider resolution, and bean reference reservations
 * when performing lightweight rule parsing.</p>
 */
public class ShallowBeanClassResolver extends BeanClassResolver {

    /**
     * Constructs a new ShallowBeanClassResolver.
     * @param ruleParsingContext the rule parsing context
     */
    public ShallowBeanClassResolver(@NonNull RuleParsingContext ruleParsingContext) {
        super(ruleParsingContext);
    }

    @Override
    public void resolveBeanClass(BeanRule beanRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveFactoryBeanClass(BeanRule beanRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveAdviceBeanClass(@NonNull AspectRule aspectRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveActionBeanClass(@NonNull InvokeActionRule invokeActionRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(@Nullable ItemRule itemRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(@Nullable Token[] tokens) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(Token token) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(@Nullable AutowireRule autowireRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(ScheduleRule scheduleRule) {
        // No-op for shallow parsing
    }

    @Override
    public void resolveBeanClass(TemplateRule templateRule) {
        // No-op for shallow parsing
    }

}
