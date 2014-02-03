package com.aspectran.core.context.aspect.pointcut;

public interface Pointcut {

	public boolean matches(String transletName);
	
	public boolean matches(String transletName, String beanOrActionId);
	
	public boolean matches(String transletName, String beanOrActionId, String beanMethodName);
	
	public boolean isActionInfluenced();
	
}
