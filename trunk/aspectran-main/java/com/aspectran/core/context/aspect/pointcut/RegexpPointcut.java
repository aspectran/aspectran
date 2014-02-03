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

}
