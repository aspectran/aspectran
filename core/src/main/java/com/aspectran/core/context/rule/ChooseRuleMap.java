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

import java.util.LinkedHashMap;

import static com.aspectran.core.context.rule.ChooseRule.toCaseGroupNo;

/**
 * <p>Created: 2019-01-06</p>
 */
public class ChooseRuleMap extends LinkedHashMap<Integer, ChooseRule> {

    private static final long serialVersionUID = 4736467004685193206L;

    public ChooseRuleMap() {
        super();
    }

    public ChooseRule newChooseRule() {
        int caseNo = toCaseGroupNo(size() + 1);
        return newChooseRule(caseNo);
    }

    public ChooseRule newChooseRule(int caseNo) {
        ChooseRule chooseRule = new ChooseRule(caseNo);
        put(caseNo, chooseRule);
        return chooseRule;
    }

    public ChooseRule getChooseRule(int caseNo) {
        return get(toCaseGroupNo(caseNo));
    }

}
