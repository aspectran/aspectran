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

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ability.Describable;
import com.aspectran.core.context.rule.ability.Replicable;

/**
 * Represents a local scope within the rule parsing process.
 * <p>This class holds contextual information that is specific to the current
 * parsing scope, such as the active {@link com.aspectran.core.context.rule.DescriptionRule}
 * and {@link DefaultSettings}. It is designed to be replicable to support nested
 * parsing contexts, for example, when one configuration file appends another.</p>
 *
 * <p>Created: 2015. 10. 2.</p>
 */
public class RuleParsingScope implements Replicable<RuleParsingScope>, Describable {

    private final RuleParsingContext ruleParsingContext;

    private DescriptionRule descriptionRule;

    private DefaultSettings defaultSettings;

    private final int nestingLevel;

    /**
     * Instantiates a new RuleParsingScope.
     * @param ruleParsingContext the rule parsing context
     */
    public RuleParsingScope(RuleParsingContext ruleParsingContext) {
        this(ruleParsingContext, 0);
    }

    private RuleParsingScope(RuleParsingContext ruleParsingContext, int nestingLevel) {
        this.ruleParsingContext = ruleParsingContext;
        this.nestingLevel = nestingLevel;
    }

    /**
     * Returns the rule parsing context.
     * @return the rule parsing context
     */
    public RuleParsingContext getRuleParsingContext() {
        return ruleParsingContext;
    }

    @Override
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    @Override
    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
        ruleParsingContext.setDescriptionRule(descriptionRule, nestingLevel);
    }

    /**
     * Returns the default settings.
     * @return the default settings
     */
    public DefaultSettings getDefaultSettings() {
        return defaultSettings;
    }

    /**
     * Returns the default settings, creating a new instance if it does not already exist.
     * @return the default settings
     */
    public DefaultSettings touchDefaultSettings() {
        if (defaultSettings == null) {
            defaultSettings = new DefaultSettings();
        }
        return defaultSettings;
    }

    /**
     * Sets the default settings.
     * @param defaultSettings the default settings
     */
    public void setDefaultSettings(DefaultSettings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    /**
     * Returns the nesting level of the current scope.
     * @return the nesting level
     */
    public int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public RuleParsingScope replicate() {
        RuleParsingScope newScope = new RuleParsingScope(ruleParsingContext, nestingLevel + 1);

        DescriptionRule dr = getDescriptionRule();
        if (dr != null) {
            newScope.setDescriptionRule(new DescriptionRule(dr));
        }

        DefaultSettings ds = getDefaultSettings();
        if (ds != null) {
            newScope.setDefaultSettings(new DefaultSettings(ds));
        }

        return newScope;
    }

}
