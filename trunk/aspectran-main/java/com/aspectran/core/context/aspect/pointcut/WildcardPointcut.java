package com.aspectran.core.context.aspect.pointcut;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranContextConstant;
import com.aspectran.core.util.wildcard.WildcardMatcher;

/**
 * The Class WildcardPointcut.
 * 
 * java.io.* : java.io 패키지 내에 속한 모든 요소
 * org.myco.myapp..* : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소
 * org.myco.myapp..*@abc.action : org.myco.myapp 패키지 또는 서브 패키지 내에 속한 모든 요소의 Action ID
 */
public class WildcardPointcut extends AbstractPointcut implements Pointcut {

	private Map<String, WildcardMatcher> reusableWildcardMatcherMap = new HashMap<String, WildcardMatcher>();
	
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
		return match(pointcutPattern.getTransletNamePattern(), transletName, AspectranContextConstant.TRANSLET_NAME_SEPARATOR);
	}	

	protected boolean matchBoth(PointcutPattern pointcutPattern, String transletName, String actionId) {
		return match(pointcutPattern.getActionIdPattern(), transletName, AspectranContextConstant.TRANSLET_NAME_SEPARATOR)
				&& match(pointcutPattern.getActionIdPattern(), actionId, AspectranContextConstant.BEAN_ID_SEPARATOR);
	}	
	
	protected boolean match(String pattern, String str, String separator) {
		WildcardMatcher wildcardMatcher = reusableWildcardMatcherMap.get(pattern);
		
		if(wildcardMatcher == null) {
			wildcardMatcher = new WildcardMatcher(pattern, separator);
			reusableWildcardMatcherMap.put(pattern, wildcardMatcher);
		}
		
		return wildcardMatcher.match(str);
	}
	
}
