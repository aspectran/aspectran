package com.aspectran.core.context.rule;

import java.util.LinkedHashMap;

import static com.aspectran.core.context.rule.ChooseRule.toCaseGroupNo;

/**
 * <p>Created: 2019-01-06</p>
 */
public class ChooseRuleMap extends LinkedHashMap<Integer, ChooseRule> {

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
