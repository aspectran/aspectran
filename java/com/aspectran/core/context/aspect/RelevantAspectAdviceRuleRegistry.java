/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.aspect;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.PointcutPatternRule;

/**
 * The Class RelevantAspectAdviceRuleRegistry.
 */
public class RelevantAspectAdviceRuleRegistry extends AspectAdviceRuleRegistry {
	
	private Map<String, AspectAdviceRuleRegistry> cache = new HashMap<String, AspectAdviceRuleRegistry>();
	
	public AspectAdviceRuleRegistry retrieve(String transletName, String beanId, String methodName) {
		String patternString = PointcutPatternRule.combinePatternString(transletName, beanId, methodName);
		AspectAdviceRuleRegistry aarr = null;

		synchronized(cache) {
			aarr = cache.get(patternString);
			
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
				
				cache.put(patternString, aarr);
			}
		}
		
		return aarr;
	}
}
