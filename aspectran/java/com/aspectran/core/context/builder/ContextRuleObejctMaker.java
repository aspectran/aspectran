package com.aspectran.core.context.builder;

import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.SettingsAdviceRule;
import com.aspectran.core.var.type.AspectAdviceType;
import com.aspectran.core.var.type.AspectTargetType;

public class ContextRuleObejctMaker {

	public static AspectRule makeAspectRule(String id, String useFor) {
		AspectTargetType aspectTargetType = null;
		
		if(useFor != null) {
			aspectTargetType = AspectTargetType.valueOf(useFor);
			
			if(aspectTargetType == null)
				throw new IllegalArgumentException("Unknown aspect target '" + useFor + "'");
		} else {
			aspectTargetType = AspectTargetType.TRANSLET;
		}
		
		AspectRule aspectRule = new AspectRule();
		aspectRule.setId(id);
		aspectRule.setAspectTargetType(aspectTargetType);
		
		return aspectRule;
	}

	public static SettingsAdviceRule makeSettingsAdviceRule(AspectRule aspectRule) {
		SettingsAdviceRule sar = new SettingsAdviceRule();
		sar.setAspectId(aspectRule.getId());
		sar.setAspectAdviceType(AspectAdviceType.SETTINGS);

		return sar;
	}
	
}
