package com.aspectran.core.context.aspect.pointcut;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.PointcutPatternRule;
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
	
	public WildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}

	public boolean matches(String transletName) {
		return matches(transletName, null, null);
	}

	public boolean matches(String transletName, String beanId) {
		return matches(transletName, beanId, null);
	}
	
	public boolean matches(String transletName, String beanId, String beanMethodName) {
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(matches(ppr, transletName, beanId, beanMethodName)) {
					List<PointcutPatternRule> epprl = ppr.getExcludePointcutPatternRuleList();
					
					if(epprl != null) {
						for(PointcutPatternRule eppr : epprl) {
							if(matches(eppr, transletName, beanId, beanMethodName)) {
								return false;
							}
						}
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 비교 항목의 값이 null이면 참으로 간주함.
	 *
	 * @param pointcutPatternRule the pointcut pattern
	 * @param transletName the translet name
	 * @param beanId the bean or action id
	 * @param beanMethodName the bean method name
	 * @return true, if successful
	 */
	protected boolean matches(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String beanMethodName) {
//		System.out.println("  transletName: " + transletName + " = " + pointcutPatternRule.getTransletNamePattern());
//		System.out.println("  beanId: " + beanId + " = " + pointcutPatternRule.getBeanIdPattern());
//		System.out.println("  beanMethodName: " + beanMethodName + " = " + pointcutPatternRule.getBeanMethodNamePattern());
//		System.out.println("+ matched: " + matched);

		if(transletName == null && pointcutPatternRule.getTransletNamePattern() != null ||
				beanId == null && pointcutPatternRule.getBeanIdPattern() != null ||
				beanMethodName == null && pointcutPatternRule.getBeanMethodNamePattern() != null)
			return false;
		
		return exists(pointcutPatternRule, transletName, beanId, beanMethodName);
	}	
	
	public boolean exists(String transletName) {
		return exists(transletName, null, null);
	}
	
	public boolean exists(String transletName, String beanId) {
		return exists(transletName, beanId, null);
	}
	
	public boolean exists(String transletName, String beanId, String beanMethodName) {
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(exists(ppr, transletName, beanId, beanMethodName)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 비교 항목의 값이 null이면 참으로 간주함.
	 *
	 * @param pointcutPatternRule the pointcut pattern
	 * @param transletName the translet name
	 * @param beanId the bean or action id
	 * @param beanMethodName the bean method name
	 * @return true, if successful
	 */
	protected boolean exists(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String beanMethodName) {
		boolean matched = true;
		
		if(transletName != null && pointcutPatternRule.getTransletNamePattern() != null)
			matched = patternMatches(pointcutPatternRule.getTransletNamePattern(), transletName, AspectranConstant.TRANSLET_NAME_SEPARATOR);
		
		if(matched && beanId != null && pointcutPatternRule.getBeanIdPattern() != null)
			matched = patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, AspectranConstant.ID_SEPARATOR);
		
		if(matched && beanMethodName != null && pointcutPatternRule.getBeanMethodNamePattern() != null)
			matched = patternMatches(pointcutPatternRule.getBeanMethodNamePattern(), beanMethodName);
		
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
