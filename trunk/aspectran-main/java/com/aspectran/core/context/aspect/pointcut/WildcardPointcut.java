package com.aspectran.core.context.aspect.pointcut;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranContextConstant;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class WildcardPointcut.
 * 
 * java.io.* : java.io 패키지 내에 속한 모든 요소
 * org.myco.myapp..* : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소
 * org.myco.myapp..*@abc.action : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소의 Action ID
 */
public class WildcardPointcut extends AbstractPointcut implements Pointcut {

	private Map<String, WildcardPattern> reusableWildcardMatcherMap = new HashMap<String, WildcardPattern>();
	
	public boolean matches(String transletName) {
		for(PointcutPattern pointcutPattern : getExcludePatternList()) {
			if(matchTransletName(pointcutPattern, transletName))
				return false;
		}
		
		for(PointcutPattern pointcutPattern : getIncludePatternList()) {
			if(matchTransletName(pointcutPattern, transletName))
				return true;
		}
		
		return false;
	}

	public boolean matches(String transletName, String actionId) {
		for(PointcutPattern pointcutPattern : getExcludePatternList()) {
			if(matchBoth(pointcutPattern, transletName, actionId))
				return false;
		}
		
		for(PointcutPattern pointcutPattern : getIncludePatternList()) {
			if(matchBoth(pointcutPattern, transletName, actionId))
				return true;
		}
		
		return false;
	}
	
	protected boolean matchTransletName(PointcutPattern pointcutPattern, String transletName) {
		return matches(pointcutPattern.getTransletNamePattern(), transletName, AspectranContextConstant.TRANSLET_NAME_SEPARATOR);
	}	

	protected boolean matchBoth(PointcutPattern pointcutPattern, String transletName, String actionId) {
		return matches(pointcutPattern.getActionIdPattern(), transletName, AspectranContextConstant.TRANSLET_NAME_SEPARATOR)
				&& matches(pointcutPattern.getActionIdPattern(), actionId, AspectranContextConstant.BEAN_ID_SEPARATOR);
	}	
	
	protected boolean matches(String pattern, String str, String separator) {
		WildcardPattern wildcardPattern = reusableWildcardMatcherMap.get(pattern);
		
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern, separator);
			reusableWildcardMatcherMap.put(pattern, wildcardPattern);
		}
		
		return wildcardPattern.matches(str);
	}
	
}
