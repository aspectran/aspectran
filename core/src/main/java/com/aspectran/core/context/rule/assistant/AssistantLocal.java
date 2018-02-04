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

import com.aspectran.core.context.rule.ability.Replicable;

/**
 * The Class AssistantLocal.
 * 
 * <p>Created: 2015. 10. 2.</p>
 */
public class AssistantLocal implements Replicable<AssistantLocal> {

    private ContextRuleAssistant assistant;

    private String description;

    private DefaultSettings defaultSettings;

    private final int replicatedCount;

    public AssistantLocal(ContextRuleAssistant assistant) {
        this(assistant, 0);
    }

    private AssistantLocal(ContextRuleAssistant assistant, int replicatedCount) {
        this.assistant = assistant;
        this.replicatedCount = replicatedCount;
    }

    public ContextRuleAssistant getAssistant() {
        return assistant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        al.setDescription(getDescription());
        DefaultSettings ds = getDefaultSettings();
        if (ds != null) {
            al.setDefaultSettings(new DefaultSettings(ds));
        }
        return al;
    }

}
