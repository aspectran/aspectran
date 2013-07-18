package com.aspectran.core.context.translet.registry;

import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;

public class TransletRuleRegistry {

	private TransletRuleMap transletRuleMap;
	
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

	public TransletRule getTransletRule(String transletName) {
		TransletRule transletRule = transletRuleMap.get(transletName);
		
		if(transletRule == null)
			throw new TransletNotFoundException();
		
		return transletRule;
	}
	
	public void destroy() {
		if(transletRuleMap != null) {
			transletRuleMap.clear();
			transletRuleMap = null;
		}
	}

}
