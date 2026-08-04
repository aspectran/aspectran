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

import java.util.HashSet;
import java.util.Set;

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

    private final int nestingLevel;

    private DescriptionRule descriptionRule;

    private DefaultSettings defaultSettings;

    private Set<String> scopedBeanIds;

    private Set<String> scopedTransletNames;

    private Set<String> scopedPropertyKeys;

    private Set<String> scopedTypeAliases;

    private Set<String> scopedAspectIds;

    private Set<String> scopedScheduleIds;

    private Set<String> scopedTemplateIds;

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

    /**
     * Returns the nesting level of the current scope.
     * @return the nesting level
     */
    public int getNestingLevel() {
        return nestingLevel;
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

    public void addScopedBeanId(String id) {
        if (id != null && !id.isEmpty()) {
            if (scopedBeanIds == null) {
                scopedBeanIds = new HashSet<>();
            }
            scopedBeanIds.add(id);
        }
    }

    public boolean hasScopedBeanId(String id) {
        return (scopedBeanIds != null && id != null && scopedBeanIds.contains(id));
    }

    public void addScopedTransletName(String name) {
        if (name != null && !name.isEmpty()) {
            if (scopedTransletNames == null) {
                scopedTransletNames = new HashSet<>();
            }
            scopedTransletNames.add(name);
        }
    }

    public boolean hasScopedTransletName(String name) {
        return (scopedTransletNames != null && name != null && scopedTransletNames.contains(name));
    }

    public void addScopedPropertyKey(String name) {
        if (name != null && !name.isEmpty()) {
            if (scopedPropertyKeys == null) {
                scopedPropertyKeys = new HashSet<>();
            }
            scopedPropertyKeys.add(name);
        }
    }

    public boolean hasScopedPropertyKey(String name) {
        return (scopedPropertyKeys != null && name != null && scopedPropertyKeys.contains(name));
    }

    public void addScopedTypeAlias(String alias) {
        if (alias != null && !alias.isEmpty()) {
            if (scopedTypeAliases == null) {
                scopedTypeAliases = new HashSet<>();
            }
            scopedTypeAliases.add(alias);
        }
    }

    public boolean hasScopedTypeAlias(String alias) {
        return (scopedTypeAliases != null && alias != null && scopedTypeAliases.contains(alias));
    }

    public void addScopedAspectId(String id) {
        if (id != null && !id.isEmpty()) {
            if (scopedAspectIds == null) {
                scopedAspectIds = new HashSet<>();
            }
            scopedAspectIds.add(id);
        }
    }

    public boolean hasScopedAspectId(String id) {
        return (scopedAspectIds != null && id != null && scopedAspectIds.contains(id));
    }

    public void addScopedScheduleId(String id) {
        if (id != null && !id.isEmpty()) {
            if (scopedScheduleIds == null) {
                scopedScheduleIds = new HashSet<>();
            }
            scopedScheduleIds.add(id);
        }
    }

    public boolean hasScopedScheduleId(String id) {
        return (scopedScheduleIds != null && id != null && scopedScheduleIds.contains(id));
    }

    public void addScopedTemplateId(String id) {
        if (id != null && !id.isEmpty()) {
            if (scopedTemplateIds == null) {
                scopedTemplateIds = new HashSet<>();
            }
            scopedTemplateIds.add(id);
        }
    }

    public boolean hasScopedTemplateId(String id) {
        return (scopedTemplateIds != null && id != null && scopedTemplateIds.contains(id));
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
