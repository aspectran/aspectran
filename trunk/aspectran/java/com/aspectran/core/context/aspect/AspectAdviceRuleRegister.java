package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

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
		register(aspectAdviceRuleRegistry, aspectRule, null);
	}
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule, AspectAdviceType withoutAspectAdviceType) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		
		if(settingsAdviceRule != null)
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(withoutAspectAdviceType == null || aspectAdviceRule.getAspectAdviceType() != withoutAspectAdviceType)
					aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
			}
		}
		
		aspectAdviceRuleRegistry.increaseAspectRuleCount();
	}
	
}
