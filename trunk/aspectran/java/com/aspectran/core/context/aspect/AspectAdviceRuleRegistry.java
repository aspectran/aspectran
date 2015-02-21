package com.aspectran.core.context.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.type.AspectAdviceType;

public class AspectAdviceRuleRegistry implements Cloneable {

	private Map<String, Object> settings;
	
	private List<AspectAdviceRule> beforeAdviceRuleList;
	
	private List<AspectAdviceRule> afterAdviceRuleList;
	
	private List<AspectAdviceRule> finallyAdviceRuleList;
	
	private List<AspectAdviceRule> exceptionRaizedAdviceRuleList;
	
	private int aspectRuleCount;

	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		return beforeAdviceRuleList;
	}

	public void setBeforeAdviceRuleList(List<AspectAdviceRule> beforeAdviceRuleList) {
		this.beforeAdviceRuleList = beforeAdviceRuleList;
	}

	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		return afterAdviceRuleList;
	}

	public void setAfterAdviceRuleList(List<AspectAdviceRule> afterAdviceRuleList) {
		this.afterAdviceRuleList = afterAdviceRuleList;
	}

	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		return finallyAdviceRuleList;
	}

	public void setFinallyAdviceRuleList(List<AspectAdviceRule> finallyAdviceRuleList) {
		this.finallyAdviceRuleList = finallyAdviceRuleList;
	}

	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		return exceptionRaizedAdviceRuleList;
	}

	public void setExceptionRaizedAdviceRuleList(List<AspectAdviceRule> exceptionRaizedAdviceRuleList) {
		this.exceptionRaizedAdviceRuleList = exceptionRaizedAdviceRuleList;
	}

	public void addAspectAdviceRule(SettingsAdviceRule settingsAdviceRule) {
		if(settingsAdviceRule.getSettings() != null) {
			if(settings == null)
				settings = new HashMap<String, Object>();

			settings.putAll(settingsAdviceRule.getSettings());
		}
	}
	
	public void addBeforeAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(beforeAdviceRuleList == null)
			beforeAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		beforeAdviceRuleList.add(aspectAdviceRule);
	}
		
	public void addAfterAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(afterAdviceRuleList == null)
			afterAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		afterAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addFinallyAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(finallyAdviceRuleList == null)
			finallyAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		finallyAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addExceptionRaizedAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(exceptionRaizedAdviceRuleList == null)
			exceptionRaizedAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		exceptionRaizedAdviceRuleList.add(0, aspectAdviceRule);
	}
	
	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
			addBeforeAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
			addAfterAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
			addFinallyAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
			addBeforeAdviceRule(aspectAdviceRule);
			addAfterAdviceRule(aspectAdviceRule);
		} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAIZED) {
			addExceptionRaizedAdviceRule(aspectAdviceRule);
		}
	}
	
	public Object getSetting(String settingName) {
		if(settings == null)
			return null;
		
		return settings.get(settingName);
	}
	
	public AspectAdviceRuleRegistry clone() throws CloneNotSupportedException {
		// shallow copy
		return (AspectAdviceRuleRegistry)super.clone();              
	}

	public int getAspectRuleCount() {
		return aspectRuleCount;
	}

	public void setAspectRuleCount(int aspectRuleCount) {
		this.aspectRuleCount = aspectRuleCount;
	}
	
	public void increaseAspectRuleCount() {
		this.aspectRuleCount++;
	}
	
}
