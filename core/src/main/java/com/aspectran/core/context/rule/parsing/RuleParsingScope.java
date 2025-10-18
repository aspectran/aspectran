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
 * The Class RuleParsingScope.
 *
 * <p>Created: 2015. 10. 2.</p>
 */
public class RuleParsingScope implements Replicable<RuleParsingScope>, Describable {

    private final RuleParsingContext ruleParsingContext;

    private DescriptionRule descriptionRule;

    private DefaultSettings defaultSettings;

    private final int replicatedCount;

    public RuleParsingScope(RuleParsingContext ruleParsingContext) {
        this(ruleParsingContext, 0);
    }

    private RuleParsingScope(RuleParsingContext ruleParsingContext, int replicatedCount) {
        this.ruleParsingContext = ruleParsingContext;
        this.replicatedCount = replicatedCount;
    }

    public RuleParsingContext getRuleParsingContext() {
        return ruleParsingContext;
    }

    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    public void setDescriptionRule(DescriptionRule descriptionRule) {
        this.descriptionRule = descriptionRule;
    }

    public DefaultSettings getDefaultSettings() {
        return defaultSettings;
    }

    public DefaultSettings touchDefaultSettings() {
        if (defaultSettings == null) {
            defaultSettings = new DefaultSettings();
        }
        return defaultSettings;
    }

    public void setDefaultSettings(DefaultSettings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    public int getReplicatedCount() {
        return replicatedCount;
    }

    @Override
    public RuleParsingScope replicate() {
        RuleParsingScope al = new RuleParsingScope(ruleParsingContext, replicatedCount + 1);

        DescriptionRule dr = getDescriptionRule();
        if (dr != null) {
            al.setDescriptionRule(new DescriptionRule(dr));
        }

        DefaultSettings ds = getDefaultSettings();
        if (ds != null) {
            al.setDefaultSettings(new DefaultSettings(ds));
        }

        return al;
    }

}
