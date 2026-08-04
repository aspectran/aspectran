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

import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.util.TextStyler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Evaluates environment profiles and merges configuration rules based on the active profiles.
 */
public class RuleProfileEvaluator {

    private final EnvironmentProfiles environmentProfiles;

    /**
     * Constructs a new RuleProfileEvaluator with the specified environment profiles.
     * @param environmentProfiles the environment profiles to evaluate against
     */
    public RuleProfileEvaluator(@Nullable EnvironmentProfiles environmentProfiles) {
        this.environmentProfiles = environmentProfiles;
    }

    /**
     * Merges two description rules based on active environment profiles.
     * @param newDr the new description rule
     * @param oldDr the old description rule
     * @return the merged description rule
     */
    public DescriptionRule merge(@NonNull DescriptionRule newDr, @Nullable DescriptionRule oldDr) {
        if (newDr.getProfiles() != null && environmentProfiles != null) {
            boolean accepted = environmentProfiles.acceptsProfiles(newDr.getProfiles());
            return mergeDescriptionRule(newDr, oldDr, accepted);
        } else {
            return mergeDescriptionRule(newDr, oldDr, true);
        }
    }

    @NonNull
    private DescriptionRule mergeDescriptionRule(@NonNull DescriptionRule newDr, @Nullable DescriptionRule oldDr, boolean accepted) {
        if (oldDr == null) {
            if (accepted) {
                if (newDr.getContent() != null) {
                    String formatted = TextStyler.styling(newDr.getContent(), newDr.getContentStyle());
                    newDr.setFormattedContent(formatted);
                }
                return newDr;
            } else {
                DescriptionRule dr = new DescriptionRule();
                dr.addCandidate(newDr);
                return dr;
            }
        }
        DescriptionRule dr = new DescriptionRule();
        if (accepted && newDr.getContent() != null) {
            String formatted = TextStyler.styling(newDr.getContent(), newDr.getContentStyle());
            if (oldDr.getFormattedContent() != null) {
                formatted = oldDr.getFormattedContent() + formatted;
            }
            dr.setFormattedContent(formatted);
        } else if (oldDr.getFormattedContent() != null) {
            dr.setFormattedContent(oldDr.getFormattedContent());
        }
        oldDr.setFormattedContent(null);
        if (oldDr.getCandidates() == null) {
            dr.addCandidate(oldDr);
        } else {
            dr.setCandidates(oldDr.getCandidates());
            oldDr.setCandidates(null);
        }
        dr.addCandidate(newDr);
        return dr;
    }

    /**
     * Merges two item rule maps based on active environment profiles.
     * @param newIrm the new item rule map
     * @param oldIrm the old item rule map
     * @return the merged item rule map
     */
    public ItemRuleMap merge(@NonNull ItemRuleMap newIrm, @Nullable ItemRuleMap oldIrm) {
        if (newIrm.getProfiles() != null && environmentProfiles != null) {
            boolean accepted = environmentProfiles.acceptsProfiles(newIrm.getProfiles());
            return mergeItemRuleMap(newIrm, oldIrm, accepted);
        } else {
            return mergeItemRuleMap(newIrm, oldIrm, true);
        }
    }

    private ItemRuleMap mergeItemRuleMap(@NonNull ItemRuleMap newIrm, @Nullable ItemRuleMap oldIrm, boolean accepted) {
        if (oldIrm == null) {
            if (accepted) {
                return newIrm;
            } else {
                ItemRuleMap irm = new ItemRuleMap();
                irm.addCandidate(newIrm);
                return irm;
            }
        }
        ItemRuleMap irm = new ItemRuleMap();
        irm.putAll(oldIrm);
        if (accepted) {
            irm.putAll(newIrm);
        }
        if (oldIrm.getCandidates() == null) {
            irm.addCandidate(oldIrm);
        } else {
            irm.setCandidates(oldIrm.getCandidates());
            oldIrm.setCandidates(null);
            oldIrm.clear();
        }
        irm.addCandidate(newIrm);
        return irm;
    }

}
