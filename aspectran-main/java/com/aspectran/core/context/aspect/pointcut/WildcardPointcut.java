package com.aspectran.core.context.aspect.pointcut;

/**
 * The Class WildcardPointcut.
 * 
 * java.io.* : java.io 패키지 내에 속한 모든 요소
 * org.myco.myapp..* : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소
 * org.myco.myapp..*@abc.action : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소의 Action ID
 */
public class WildcardPointcut extends AbstractPointcut implements Pointcut {

	public boolean matches(String transletName) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean matches(String transletName, String actionId) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
