package com.aspectran.core.context.aspect;

import java.util.List;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

public class AspectAdviceRuleRegister {
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule) {
		register(aspectAdviceRuleRegistry, aspectRule, null);
	}
	
	public static void register(AspectAdviceRuleRegistry aspectAdviceRuleRegistry, AspectRule aspectRule, AspectAdviceType excludeAspectAdviceType) {
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		
		if(settingsAdviceRule != null)
			aspectAdviceRuleRegistry.addAspectAdviceRule(settingsAdviceRule);
		
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(excludeAspectAdviceType == null || aspectAdviceRule.getAspectAdviceType() != excludeAspectAdviceType)
					aspectAdviceRuleRegistry.addAspectAdviceRule(aspectAdviceRule);
			}
		}
		
		aspectAdviceRuleRegistry.increaseAspectRuleCount();
	}
	
}
