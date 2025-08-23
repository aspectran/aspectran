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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.rule.AspectRule;

/**
 * A helper class that facilitates the registration of aspect rules into an
 * {@link AdviceRuleRegistry}. The registry is created lazily on the first
 * registration attempt.
 * <p>This is useful for collecting all relevant aspect rules before constructing
 * the final advice execution chain.
 * </p>
 */
public class AdviceRulePostRegister {

    private AdviceRuleRegistry adviceRuleRegistry;

    /**
     * Registers the advice and exception rules from the given {@link AspectRule}.
     * If this is the first rule being registered, a new {@link AdviceRuleRegistry} is created.
     * @param aspectRule the aspect rule to register
     */
    public void register(AspectRule aspectRule) {
        if (aspectRule != null) {
            if (adviceRuleRegistry == null) {
                adviceRuleRegistry = new AdviceRuleRegistry();
            }
            adviceRuleRegistry.register(aspectRule);
        }
    }

    /**
     * Returns the internal {@link AdviceRuleRegistry} that contains all the registered rules.
     * @return the advice rule registry, or {@code null} if no rules have been registered
     */
    public AdviceRuleRegistry getAdviceRuleRegistry() {
        return adviceRuleRegistry;
    }

}
