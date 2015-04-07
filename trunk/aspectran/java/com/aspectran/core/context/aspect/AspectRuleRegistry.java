package com.aspectran.core.context.aspect;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AspectRuleMap;

/**
 * The Class AspectRuleRegistry.
 */
public class AspectRuleRegistry {

	private final AspectRuleMap aspectRuleMap;
	
	public AspectRuleRegistry(AspectRuleMap aspectRuleMap) {
		this.aspectRuleMap = aspectRuleMap;
	}
	
	/**
	 * Gets the aspect rule map.
	 * 
	 * @return the aspect rule map
	 */
	public AspectRuleMap getAspectRuleMap() {
		return aspectRuleMap;
	}

	public boolean contains(String aspectId) {
		return aspectRuleMap.containsKey(aspectId);
	}
	
	public AspectRule getAspectRule(String aspectId) {
		return aspectRuleMap.get(aspectId);
	}

	public void destroy() {
		if(aspectRuleMap != null) {
			aspectRuleMap.clear();
		}
	}

}
