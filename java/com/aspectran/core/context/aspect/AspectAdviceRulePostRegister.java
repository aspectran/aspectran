package com.aspectran.core.context.aspect;

import com.aspectran.core.context.rule.AspectRule;

/**
 * The Class AspectAdviceRulePostRegister.
 */
public class AspectAdviceRulePostRegister extends AspectAdviceRuleRegister {
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	public AspectAdviceRuleRegistry register(AspectRule aspectRule) {
		if(aspectAdviceRuleRegistry == null)
			aspectAdviceRuleRegistry = new AspectAdviceRuleRegistry();
		
		register(aspectAdviceRuleRegistry, aspectRule);
		
		return aspectAdviceRuleRegistry;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}
	
}