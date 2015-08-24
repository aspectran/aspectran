/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.util.List;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.AspectRule;

/**
 * The Class RelevantAspectRuleHolder.
 */
public class RelevantAspectRuleHolder {

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	private List<AspectRule> activityAspectRuleList;

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(
			AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}

	public List<AspectRule> getActivityAspectRuleList() {
		return activityAspectRuleList;
	}

	public void setActivityAspectRuleList(List<AspectRule> activityAspectRuleList) {
		this.activityAspectRuleList = activityAspectRuleList;
	}
	
}
