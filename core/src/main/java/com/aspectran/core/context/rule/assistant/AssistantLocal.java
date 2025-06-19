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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ability.Replicable;

/**
 * The Class AssistantLocal.
 * 
 * <p>Created: 2015. 10. 2.</p>
 */
public class AssistantLocal implements Replicable<AssistantLocal> {

    private final ActivityRuleAssistant assistant;

    private DescriptionRule descriptionRule;

    private DefaultSettings defaultSettings;

    private final int replicatedCount;

    public AssistantLocal(ActivityRuleAssistant assistant) {
        this(assistant, 0);
    }

    private AssistantLocal(ActivityRuleAssistant assistant, int replicatedCount) {
        this.assistant = assistant;
        this.replicatedCount = replicatedCount;
    }

    public ActivityRuleAssistant getAssistant() {
        return assistant;
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
    public AssistantLocal replicate() {
        AssistantLocal al = new AssistantLocal(assistant, replicatedCount + 1);

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
