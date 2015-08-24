package com.aspectran.core.context.translet;

import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;

public class TransletRuleRegistry {

	private final TransletRuleMap transletRuleMap;
	
	public TransletRuleRegistry(TransletRuleMap transletRuleMap) {
		this.transletRuleMap = transletRuleMap;
	}
	
	/**
	 * Gets the translet rule map.
	 * 
	 * @return the translet rule map
	 */
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	public boolean contains(String transletName) {
		return transletRuleMap.containsKey(transletName);
	}
	
	public TransletRule getTransletRule(String transletName) {
		return transletRuleMap.get(transletName);
	}
	
	public void destroy() {
		if(transletRuleMap != null) {
			transletRuleMap.clear();
		}
	}

}
