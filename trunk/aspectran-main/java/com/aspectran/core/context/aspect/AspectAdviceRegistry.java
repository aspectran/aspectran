package com.aspectran.core.context.aspect;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.type.AspectAdviceType;

public class AspectAdviceRegistry {

	private List<AspectAdviceRule> beforeAdviceRuleList;
	
	private List<AspectAdviceRule> afterAdviceRuleList;
	
	private List<AspectAdviceRule> finallyAdviceRuleList;
	
	private List<AspectAdviceRule> exceptionRaizedAdviceRuleList;

	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		return beforeAdviceRuleList;
	}

	public void setBeforeAdviceRuleList(List<AspectAdviceRule> beforeAdviceRuleList) {
		this.beforeAdviceRuleList = beforeAdviceRuleList;
	}

	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		return afterAdviceRuleList;
	}

	public void setAfterAdviceRuleList(List<AspectAdviceRule> afterAdviceRuleList) {
		this.afterAdviceRuleList = afterAdviceRuleList;
	}

	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		return finallyAdviceRuleList;
	}

	public void setFinallyAdviceRuleList(List<AspectAdviceRule> finallyAdviceRuleList) {
		this.finallyAdviceRuleList = finallyAdviceRuleList;
	}

	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		return exceptionRaizedAdviceRuleList;
	}

	public void setExceptionRaizedAdviceRuleList(List<AspectAdviceRule> exceptionRaizedAdviceRuleList) {
		this.exceptionRaizedAdviceRuleList = exceptionRaizedAdviceRuleList;
	}

	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
			if(beforeAdviceRuleList == null)
				beforeAdviceRuleList = new ArrayList<AspectAdviceRule>();
			beforeAdviceRuleList.add(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
			if(afterAdviceRuleList == null)
				afterAdviceRuleList = new ArrayList<AspectAdviceRule>();
			afterAdviceRuleList.add(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
			if(finallyAdviceRuleList == null)
				finallyAdviceRuleList = new ArrayList<AspectAdviceRule>();
			finallyAdviceRuleList.add(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
			if(beforeAdviceRuleList == null)
				beforeAdviceRuleList = new ArrayList<AspectAdviceRule>();
			beforeAdviceRuleList.add(aspectAdviceRule);
			if(afterAdviceRuleList == null)
				afterAdviceRuleList = new ArrayList<AspectAdviceRule>();
			afterAdviceRuleList.add(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAIZED) {
			if(exceptionRaizedAdviceRuleList == null)
				exceptionRaizedAdviceRuleList = new ArrayList<AspectAdviceRule>();
			exceptionRaizedAdviceRuleList.add(aspectAdviceRule);
		}
	}
	
}
