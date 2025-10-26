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
 * A holder for aspect rules that are relevant to a specific join point.
 *
 * <p>This object is cached by the {@link AspectRuleRegistry} to improve
 * performance. It separates statically-defined advice (pre-organized in an
 * {@link AdviceRuleRegistry}) from dynamic aspect rules that may need further
 * evaluation at the join point.
 * </p>
 */
public class RelevantAspectRuleHolder {

    private AdviceRuleRegistry adviceRuleRegistry;

    private List<AspectRule> dynamicAspectRuleList;

    /**
     * Returns the registry containing advice rules that are statically determined
     * to be relevant.
     * @return the advice rule registry
     */
    public AdviceRuleRegistry getAdviceRuleRegistry() {
        return adviceRuleRegistry;
    }

    /**
     * Sets the registry for statically relevant advice rules.
     * @param adviceRuleRegistry the new advice rule registry
     */
    public void setAdviceRuleRegistry(AdviceRuleRegistry adviceRuleRegistry) {
        this.adviceRuleRegistry = adviceRuleRegistry;
    }

    /**
     * Returns the list of aspect rules that require dynamic evaluation to determine
     * if they apply to the join point.
     * @return the dynamic aspect rule list
     */
    public List<AspectRule> getDynamicAspectRuleList() {
        return dynamicAspectRuleList;
    }

    /**
     * Sets the list of dynamically relevant aspect rules.
     * @param dynamicAspectRuleList the new dynamic aspect rule list
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
