package com.aspectran.core.context.aspect;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.rule.AspectAdviceRule;
import com.aspectran.core.rule.AspectSettingsAdviceRule;
import com.aspectran.core.type.AspectAdviceType;

public class AspectAdvice {

	private Map<String, Object> settings;
	
	private AspectAdviceRule beforeAdviceRule;
	
	private AspectAdviceRule afterAdviceRule;
	
	private AspectAdviceRule finallyAdviceRule;
	
	private AspectAdviceRule exceptionRaizedAdviceRule;

	public AspectAdviceRule getBeforeAdviceRule() {
		return beforeAdviceRule;
	}

	public void setBeforeAdviceRule(AspectAdviceRule beforeAdviceRule) {
		this.beforeAdviceRule = beforeAdviceRule;
	}

	public AspectAdviceRule getAfterAdviceRule() {
		return afterAdviceRule;
	}

	public void setAfterAdviceRule(AspectAdviceRule afterAdviceRule) {
		this.afterAdviceRule = afterAdviceRule;
	}

	public AspectAdviceRule getFinallyAdviceRule() {
		return finallyAdviceRule;
	}

	public void setFinallyAdviceRule(AspectAdviceRule finallyAdviceRule) {
		this.finallyAdviceRule = finallyAdviceRule;
	}

	public AspectAdviceRule getExceptionRaizedAdviceRule() {
		return exceptionRaizedAdviceRule;
	}

	public void setExceptionRaizedAdviceRule(AspectAdviceRule exceptionRaizedAdviceRule) {
		this.exceptionRaizedAdviceRule = exceptionRaizedAdviceRule;
	}

	public void addAspectAdviceRule(AspectSettingsAdviceRule aspectSettingsAdviceRule) {
		if(settings == null)
			settings = new HashMap<String, Object>();

		if(aspectSettingsAdviceRule.getSettings() != null)
			settings.putAll(aspectSettingsAdviceRule.getSettings());
	}
		
	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
			beforeAdviceRule = aspectAdviceRule;
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
			afterAdviceRule = aspectAdviceRule;
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
			finallyAdviceRule = aspectAdviceRule;
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
			beforeAdviceRule = aspectAdviceRule;
			afterAdviceRule = aspectAdviceRule;
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAIZED) {
			exceptionRaizedAdviceRule = aspectAdviceRule;
		}
	}
	
	public Object getSetting(String settingName) {
		if(settings == null)
			return null;
		
		return settings.get(settingName);
	}
	
}
