package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.var.rule.PointcutPatternRule;


public abstract class AbstractPointcut {
	
	protected final List<PointcutPatternRule> pointcutPatternRuleList;

	public AbstractPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		this.pointcutPatternRuleList = pointcutPatternRuleList;
	}
	
	public List<PointcutPatternRule> getPointcutPatternRuleList() {
		return pointcutPatternRuleList;
	}

	public void addPointcutPatternRule(List<PointcutPatternRule> pointcutPatternList) {
		if(pointcutPatternList == null) {
			pointcutPatternList = new ArrayList<PointcutPatternRule>(pointcutPatternList);
			return;
		}

		pointcutPatternList.addAll(pointcutPatternList);
	}
	
}
