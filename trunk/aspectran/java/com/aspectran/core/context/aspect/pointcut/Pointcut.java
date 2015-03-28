package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.context.rule.PointcutPatternRule;

public interface Pointcut {
	
	public List<PointcutPatternRule> getPointcutPatternRuleList();
	
	public boolean matches(String transletName);
	
	public boolean matches(String transletName, String beanId);
	
	public boolean matches(String transletName, String beanId, String beanMethodName);

	public boolean exists(String transletName);
	
	public boolean exists(String transletName, String beanId);
	
	public boolean exists(String transletName, String beanId, String beanMethodName);
	
	public void clearCache();
}
