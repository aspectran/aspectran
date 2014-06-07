package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

public interface Pointcut {
	
	public List<PointcutPattern> getPointcutPatternList();
	
	public void addPointcutPattern(PointcutPattern pointcutPattern);
	
	public void addPointcutPattern(List<PointcutPattern> pointcutPatternList);

	public boolean matches(String transletName);
	
	public boolean matches(String transletName, String beanOrActionId);
	
	public boolean matches(String transletName, String beanOrActionId, String beanMethodName);
	
	public boolean strictMatches(String transletName);
	
	public boolean strictMatches(String transletName, String beanOrActionId);
	
	public boolean strictMatches(String transletName, String beanOrActionId, String beanMethodName);

	public void clearCache();
}
