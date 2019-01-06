package com.aspectran.core.context.rule;

import java.util.LinkedHashMap;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseRuleMap extends LinkedHashMap<Integer, CaseRule> {

    public CaseRuleMap() {
        super();
    }

    public CaseRule newCaseRule() {
        int caseNo = size() + 1;
        CaseRule caseRule = new CaseRule(caseNo);
        put(caseNo, caseRule);
        return caseRule;
    }

    public CaseRule getCaseRule(int caseNo) {
        if (caseNo > 1000) {
            caseNo = caseNo / 1000;
        }
        return get(caseNo);
    }

}
