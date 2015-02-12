package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.context.rule.PointcutPatternRule;

public interface Pointcut {
	
	public List<PointcutPatternRule> getPointcutPatternRuleList();
	
	public boolean matches(String transletName);
	
	public boolean matches(String transletName, String beanOrActionId);
	
	public boolean matches(String transletName, String beanOrActionId, String beanMethodName);
	
	public boolean strictMatches(String transletName);
	
	public boolean strictMatches(String transletName, String beanOrActionId);
	
	public boolean strictMatches(String transletName, String beanOrActionId, String beanMethodName);

	public void clearCache();
}
