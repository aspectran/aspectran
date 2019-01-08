package com.aspectran.core.context.rule;

import java.util.LinkedHashMap;

import static com.aspectran.core.context.rule.CaseRule.toCaseGroupNo;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseRuleMap extends LinkedHashMap<Integer, CaseRule> {

    public CaseRuleMap() {
        super();
    }

    public CaseRule newCaseRule() {
        int caseNo = toCaseGroupNo(size() + 1);
        return newCaseRule(caseNo);
    }

    public CaseRule newCaseRule(int caseNo) {
        CaseRule caseRule = new CaseRule(caseNo);
        put(caseNo, caseRule);
        return caseRule;
    }

    public CaseRule getCaseRule(int caseNo) {
        return get(toCaseGroupNo(caseNo));
    }

}
