package com.aspectran.core.context.aspect.pointcut;

public class RegexpPointcut extends AbstractPointcut implements Pointcut {

	public boolean matches(String transletName) {
		return false;
	}

	public boolean matches(String transletName, String beanOrActionId) {
		return false;
	}

	public boolean matches(String transletName, String beanOrActionId, String beanMethodName) {
		return false;
	}
	
	public boolean strictMatches(String transletName) {
		return false;
	}

	public boolean strictMatches(String transletName, String beanOrActionId) {
		return false;
	}

	public boolean strictMatches(String transletName, String beanOrActionId, String beanMethodName) {
		return false;
	}
	
	public void clearCache() {
		
	}

}
