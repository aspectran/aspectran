/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.ActionRuleApplicable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2019-01-06</p>
 */
public class ChooseRule {

    private final int caseNo;

    private Map<Integer, ChooseWhenRule> chooseWhenRuleMap;

    public ChooseRule(int caseNo) {
        checkCaseNo(caseNo);
        this.caseNo = caseNo;
    }

    public int getCaseNo() {
        return caseNo;
    }

    public ChooseWhenRule newChooseWhenRule() {
        int size = (chooseWhenRuleMap != null ? chooseWhenRuleMap.size() : 0);
        return newChooseWhenRule(caseNo + size + 1);
    }

    public ChooseWhenRule newChooseWhenRule(int caseNo) {
        ChooseWhenRule choosehenRule = new ChooseWhenRule(caseNo);
        if (chooseWhenRuleMap == null) {
            chooseWhenRuleMap = new LinkedHashMap<>();
        }
        chooseWhenRuleMap.put(caseNo, choosehenRule);
        return choosehenRule;
    }

    public ChooseWhenRule getChooseWhenRule(int caseNo) {
        return (chooseWhenRuleMap != null ? chooseWhenRuleMap.get(caseNo) : null);
    }

    public Map<Integer, ChooseWhenRule> getChooseWhenRuleMap() {
        return chooseWhenRuleMap;
    }

    public void join(ActionRuleApplicable applicable) {
        if (chooseWhenRuleMap != null) {
            for (ChooseWhenRule chooseWhenRule : chooseWhenRuleMap.values()) {
                chooseWhenRule.join(applicable);
            }
        }
    }

    public static int toCaseGroupNo(int caseNo) {
        if (caseNo < 1000) {
            return (caseNo * 1000);
        }
        checkCaseNo(caseNo);
        return (caseNo / 1000 * 1000);
    }

    public static void checkCaseNo(int caseNo) {
        if (caseNo < 1000 || caseNo > 999999) {
            throw new IllegalArgumentException("caseNo must be > 1000 and <= 999999");
        }
    }

}
