package com.aspectran.core.context.aspect.pointcut;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

	private Map<String, WildcardPattern> wildcardPatternCache = new WeakHashMap<String, WildcardPattern>();
	
	public boolean matches(String transletName) {
		return matches(transletName, null, null);
	}

	public boolean matches(String transletName, String beanOrActionId) {
		return matches(transletName, beanOrActionId, null);
	}
	
	public boolean matches(String transletName, String beanOrActionId, String beanMethodName) {
		if(pointcutPatternList != null) {
			for(PointcutPattern pp : pointcutPatternList) {
				if(matches(pp, transletName, beanOrActionId, beanMethodName)) {
					List<PointcutPattern> mppl = pp.getMinusPointcutPatternList();
					
					if(mppl != null) {
						for(PointcutPattern wpp : mppl) {
							if(matches(wpp, transletName, beanOrActionId, beanMethodName)) {
								return false;
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	protected boolean matches(PointcutPattern pointcutPattern, String transletName) {
		return matches(pointcutPattern, transletName, null, null);
	}	

	protected boolean matches(PointcutPattern pointcutPattern, String transletName, String beanOrActionId) {
		return matches(pointcutPattern, transletName, beanOrActionId, null);
	}	
	
	/**
	 * Matches.
	 * 비교 항목의 값이 null이면 참으로 간주함.
	 *
	 * @param pointcutPattern the pointcut pattern
	 * @param transletName the translet name
	 * @param beanOrActionId the bean or action id
	 * @param beanMethodName the bean method name
	 * @return true, if successful
	 */
	protected boolean matches(PointcutPattern pointcutPattern, String transletName, String beanOrActionId, String beanMethodName) {
		boolean matched = true;
		
		if(transletName != null && pointcutPattern.getTransletNamePattern() != null)
			matched = patternMatches(pointcutPattern.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR);

		if(matched && beanOrActionId != null && pointcutPattern.getBeanOrActionIdPattern() != null)
			matched = patternMatches(pointcutPattern.getBeanOrActionIdPattern(), beanOrActionId, AspectranConstant.ID_SEPARATOR);
		
		if(matched && beanMethodName != null && pointcutPattern.getBeanMethodNamePattern() != null)
			matched = patternMatches(pointcutPattern.getBeanMethodNamePattern(), beanMethodName);
		
		return matched;
	}	
	
	public boolean strictMatches(String transletName) {
		return strictMatches(transletName, null, null);
	}

	public boolean strictMatches(String transletName, String beanOrActionId) {
		return strictMatches(transletName, beanOrActionId, null);
	}
	
	public boolean strictMatches(String transletName, String beanOrActionId, String beanMethodName) {
		if(pointcutPatternList != null) {
			for(PointcutPattern pp : pointcutPatternList) {
				if(strictMatches(pp, transletName, beanOrActionId, beanMethodName)) {
					List<PointcutPattern> wppl = pp.getMinusPointcutPatternList();
					
					if(wppl != null) {
						for(PointcutPattern wpp : wppl) {
							if(strictMatches(wpp, transletName, beanOrActionId, beanMethodName)) {
								return false;
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	protected boolean strictMatches(PointcutPattern pointcutPattern, String transletName) {
		return strictMatches(pointcutPattern, transletName, null, null);
	}	

	protected boolean strictMatches(PointcutPattern pointcutPattern, String transletName, String beanOrActionId) {
		return strictMatches(pointcutPattern, transletName, beanOrActionId, null);
	}	
	
	protected boolean strictMatches(PointcutPattern pointcutPattern, String transletName, String beanOrActionId, String beanMethodName) {
		boolean matched = true;
		
		if(pointcutPattern.getTransletNamePattern() != null) {
			if(transletName == null) {
				matched = false;
			} else {
				matched = patternMatches(pointcutPattern.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR);				
			}
		}

		if(matched && pointcutPattern.getBeanOrActionIdPattern() != null) {
			if(beanOrActionId == null) {
				matched = false;
			} else {
				matched = patternMatches(pointcutPattern.getBeanOrActionIdPattern(), beanOrActionId, AspectranConstant.ID_SEPARATOR);				
			}
		}

		if(matched && pointcutPattern.getBeanMethodNamePattern() != null) {
			if(beanMethodName == null) {
				matched = false;
			} else {
				matched = patternMatches(pointcutPattern.getBeanMethodNamePattern(), beanMethodName);				
			}
		}
		
		return matched;
	}	
	
	protected boolean patternMatches(String pattern, String str) {
		WildcardPattern wildcardPattern;
		
		synchronized(wildcardPatternCache) {
			wildcardPattern = wildcardPatternCache.get(pattern);
			
			if(wildcardPattern == null) {
				wildcardPattern = new WildcardPattern(pattern);
				wildcardPatternCache.put(pattern, wildcardPattern);
			}
		}

		//System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);
	}
	
	protected boolean patternMatches(String pattern, String str, String separator) {
		String patternId = pattern + separator;
		
		WildcardPattern wildcardPattern;
		
		synchronized(wildcardPatternCache) {
			wildcardPattern = wildcardPatternCache.get(patternId);
			
			if(wildcardPattern == null) {
				wildcardPattern = new WildcardPattern(pattern, separator);
				wildcardPatternCache.put(patternId, wildcardPattern);
			}
		}

		//System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);
	}
	
	protected boolean patternMatches(String pattern, String str, char separator) {
		String patternId = pattern + separator;
		
		WildcardPattern wildcardPattern;
		
		synchronized(wildcardPatternCache) {
			wildcardPattern = wildcardPatternCache.get(patternId);
			
			if(wildcardPattern == null) {
				wildcardPattern = new WildcardPattern(pattern, separator);
				wildcardPatternCache.put(patternId, wildcardPattern);
			}
		}

		//System.out.println("pattern:" + pattern + " str:" + str + " result:" + wildcardPattern.matches(str));

		return wildcardPattern.matches(str);
	}
	
	public void clearCache() {
		wildcardPatternCache.clear();
	}
	
}
