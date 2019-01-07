package com.aspectran.core.context.rule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseRule {

    private final int caseNo;

    private Map<Integer, CaseWhenRule> caseWhenRuleMap;

    public CaseRule(int caseNo) {
        this.caseNo = caseNo;
    }

    public int getCaseNo() {
        return caseNo;
    }

    public CaseWhenRule newCaseWhenRule() {
        int size = (caseWhenRuleMap != null ? caseWhenRuleMap.size() : 0);
        int caseWhenNo = (caseNo * 1000) + size + 1;
        return newCaseWhenRule(caseWhenNo);
    }

    public CaseWhenRule newCaseWhenRule(int caseWhenNo) {
        if (caseWhenRuleMap == null) {
            caseWhenRuleMap = new LinkedHashMap<>();
        }
        CaseWhenRule caseWhenRule = new CaseWhenRule(caseWhenNo);
        caseWhenRuleMap.put(caseWhenNo, caseWhenRule);
        return caseWhenRule;
    }

    public CaseWhenRule getCaseWhenRule(int caseWhenNo) {
        return (caseWhenRuleMap != null ? caseWhenRuleMap.get(caseWhenNo) : null);
    }

    public Map<Integer, CaseWhenRule> getCaseWhenRuleMap() {
        return caseWhenRuleMap;
    }

    public static int toCaseNo(int caseNo) {
        if (caseNo > 1000) {
            caseNo = caseNo / 1000;
        }
        return caseNo;
    }

}
