package com.aspectran.core.context.aspect.pointcut;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class WildcardPointcut.
 * 
 * java.io.* : java.io 패키지 내에 속한 모든 요소
 * org.myco.myapp..* : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소
 * org.myco.myapp..*@abc.action : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소의 Action ID
 */
public class WildcardPointcut extends AbstractPointcut implements Pointcut {

	private Map<String, WildcardPattern> reusableWildcardPatternMap = new HashMap<String, WildcardPattern>();
	
	public boolean matches(String transletName) {
		if(getExcludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getExcludePatternList()) {
				if(matches(pointcutPattern, transletName))
					return false;
			}
		}
			
		if(getIncludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getIncludePatternList()) {
				if(matches(pointcutPattern, transletName))
					return true;
			}
		}
		
		return false;
	}

	public boolean matches(String transletName, String actionName) {
		if(getExcludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getExcludePatternList()) {
				if(matches(pointcutPattern, transletName, actionName))
					return false;
			}
		}
		
		if(getIncludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getIncludePatternList()) {
				if(matches(pointcutPattern, transletName, actionName))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean matches(String transletName, String actionName, String beanMethodName) {
		if(getExcludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getExcludePatternList()) {
				if(matches(pointcutPattern, transletName, actionName, beanMethodName))
					return false;
			}
		}
		
		if(getIncludePatternList() != null) {
			for(PointcutPattern pointcutPattern : getIncludePatternList()) {
				if(matches(pointcutPattern, transletName, actionName, beanMethodName))
					return true;
			}
		}
		
		return false;
	}
	
	protected boolean matches(PointcutPattern pointcutPattern, String transletName) {
		return patternMatches(pointcutPattern.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR);
	}	

	protected boolean matches(PointcutPattern pointcutPattern, String transletName, String actionName) {
		return patternMatches(pointcutPattern.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR)
				&& patternMatches(pointcutPattern.getActionNamePattern(), actionName, AspectranConstant.ACTION_ID_SEPARATOR);
	}	
	
	protected boolean matches(PointcutPattern pointcutPattern, String transletName, String actionName, String beanMethodName) {
		return patternMatches(pointcutPattern.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR)
				&& patternMatches(pointcutPattern.getActionNamePattern(), actionName, AspectranConstant.BEAN_ID_SEPARATOR)
				&& patternMatches(pointcutPattern.getBeanMethodNamePattern(), beanMethodName);
	}	
	
	protected boolean patternMatches(String pattern, String str) {
		WildcardPattern wildcardPattern = reusableWildcardPatternMap.get(pattern);
		
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern);
			reusableWildcardPatternMap.put(pattern, wildcardPattern);
		}

		System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);	}
	
	protected boolean patternMatches(String pattern, String str, String separator) {
		String patternId = pattern + separator;
		
		WildcardPattern wildcardPattern = reusableWildcardPatternMap.get(patternId);
		
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern, separator);
			reusableWildcardPatternMap.put(patternId, wildcardPattern);
		}

		System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);
	}
	
	protected boolean patternMatches(String pattern, String str, char separator) {
		String patternId = pattern + separator;
		
		WildcardPattern wildcardPattern = reusableWildcardPatternMap.get(patternId);
		
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern, separator);
			reusableWildcardPatternMap.put(patternId, wildcardPattern);
		}

		System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);
	}
	
}
