package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.SettingsAdviceRule;

public class AspectAdviceRuleRegister {
	
	
//	protected void register(BeanRule beanRule, AspectRule aspectRule) {
//		RelevantAspectAdviceRuleRegistry raarr = beanRule.getRelevantAspectAdviceRuleRegistry();
//		
//		if(raarr == null) {
//			raarr = new RelevantAspectAdviceRuleRegistry();
//			beanRule.setRelevantAspectAdviceRuleRegistry(raarr);
//		}
//		
//		register(raarr, aspectRule);
//	}
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		
		if(settingsAdviceRule != null)
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
			}
		}
		
		aspectAdviceRuleRegistry.increaseAspectRuleCount();
	}
	
}
