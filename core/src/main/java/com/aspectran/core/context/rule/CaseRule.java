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
        checkCaseNo(caseNo);
        this.caseNo = caseNo;
    }

    public int getCaseNo() {
        return caseNo;
    }

    public CaseWhenRule newCaseWhenRule() {
        int size = (caseWhenRuleMap != null ? caseWhenRuleMap.size() : 0);
        return newCaseWhenRule(caseNo + size + 1);
    }

    public CaseWhenRule newCaseWhenRule(int caseNo) {
        CaseWhenRule caseWhenRule = new CaseWhenRule(caseNo);
        if (caseWhenRuleMap == null) {
            caseWhenRuleMap = new LinkedHashMap<>();
        }
        caseWhenRuleMap.put(caseNo, caseWhenRule);
        return caseWhenRule;
    }

    public CaseWhenRule getCaseWhenRule(int caseNo) {
        return (caseWhenRuleMap != null ? caseWhenRuleMap.get(caseNo) : null);
    }

    public Map<Integer, CaseWhenRule> getCaseWhenRuleMap() {
        return caseWhenRuleMap;
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
