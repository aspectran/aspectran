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
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class RelevantAspectRuleHolder.
 */
public class RelevantAspectRuleHolder {

    private AdviceRuleRegistry adviceRuleRegistry;

    private List<AspectRule> dynamicAspectRuleList;

    /**
     * Gets the advice rule registry.
     * @return the advice rule registry
     */
    public AdviceRuleRegistry getAdviceRuleRegistry() {
        return adviceRuleRegistry;
    }

    /**
     * Sets the advice rule registry.
     * @param adviceRuleRegistry the new advice rule registry
     */
    public void setAdviceRuleRegistry(AdviceRuleRegistry adviceRuleRegistry) {
        this.adviceRuleRegistry = adviceRuleRegistry;
    }

    /**
     * Gets the dynamic aspect rule list.
     * @return the dynamic aspect rule list
     */
    public List<AspectRule> getDynamicAspectRuleList() {
        return dynamicAspectRuleList;
    }

    /**
     * Sets the relevant aspect rule list.
     * @param dynamicAspectRuleList the new relevant aspect rule list
     */
    public void setDynamicAspectRuleList(List<AspectRule> dynamicAspectRuleList) {
        this.dynamicAspectRuleList = dynamicAspectRuleList;
    }

    @Nullable
    private List<String> getDynamicAspectIds() {
        if (dynamicAspectRuleList != null && !dynamicAspectRuleList.isEmpty()) {
            List<String> aspectIds = new ArrayList<>(dynamicAspectRuleList.size());
            for (AspectRule aspectRule : dynamicAspectRuleList) {
                aspectIds.add(aspectRule.getId());
            }
            return aspectIds;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("adviceRuleRegistry", adviceRuleRegistry);
        tsb.append("dynamicAspectRules", getDynamicAspectIds());
        return tsb.toString();
    }

}
