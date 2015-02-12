package com.aspectran.core.context.aspect;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.PointcutPatternRule;


public class RelevantAspectAdviceRuleRegistry extends AspectAdviceRuleRegistry {
	
	private Map<String, AspectAdviceRuleRegistry> aspectAdviceRuleRegistryCache = new HashMap<String, AspectAdviceRuleRegistry>();
	
	public AspectAdviceRuleRegistry retrieve(String transletName, String beanId, String methodName) {
		String patternString = PointcutPatternRule.combinePatternString(transletName, beanId, methodName);
		AspectAdviceRuleRegistry aarr = null;

		synchronized(aspectAdviceRuleRegistryCache) {
			aarr = aspectAdviceRuleRegistryCache.get(patternString);
			
			if(aarr == null) {
				aarr = new AspectAdviceRuleRegistry();
				
				for(AspectAdviceRule aspectAdviceRule : getBeforeAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aarr.addBeforeAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getAfterAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aarr.addAfterAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getFinallyAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aarr.addFinallyAdviceRule(aspectAdviceRule);
				}
				
				for(AspectAdviceRule aspectAdviceRule : getExceptionRaizedAdviceRuleList()) {
					AspectRule aspectRule = aspectAdviceRule.getAspectRule();
					Pointcut pointcut = aspectRule.getPointcut();
					
					if(pointcut == null || pointcut.matches(transletName, beanId, methodName))
						aarr.addExceptionRaizedAdviceRule(aspectAdviceRule);
				}
				
				aspectAdviceRuleRegistryCache.put(patternString, aarr);
			}
		}
		
		return aarr;
	}
}
