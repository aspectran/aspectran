package com.aspectran.core.context.aspect;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.aspect.pointcut.PointcutPattern;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectRule;


public class DynamicAspectAdviceRuleRegistry extends AspectAdviceRuleRegistry {
	
	private Map<String, AspectAdviceRuleRegistry> aspectAdviceRuleRegistryCache = new HashMap<String, AspectAdviceRuleRegistry>();

	public AspectAdviceRuleRegistry getMatchAspectAdviceRuleRegistry(String transletName, String beanId, String methodName) {
		String patternString = PointcutPattern.combinePatternString(transletName, beanId, methodName);
		AspectAdviceRuleRegistry aspectAdviceRuleRegistry = null;

		synchronized(aspectAdviceRuleRegistryCache) {
			aspectAdviceRuleRegistry = aspectAdviceRuleRegistryCache.get(patternString);
			
			if(aspectAdviceRuleRegistry == null) {
				aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
				
				for(AspectAdviceRule aspectAdviceRule : getBeforeAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aspectAdviceRuleRegistry.addBeforeAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getAfterAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aspectAdviceRuleRegistry.addAfterAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getFinallyAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aspectAdviceRuleRegistry.addFinallyAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getExceptionRaizedAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aspectAdviceRuleRegistry.addExceptionRaizedAdviceRule(aspectAdviceRule);
				}
				
				aspectAdviceRuleRegistryCache.put(patternString, aspectAdviceRuleRegistry);
			}
		}
		
		return aspectAdviceRuleRegistry;
	}
}
