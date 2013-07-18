package com.aspectran.core.context.translet.registry;

import com.aspectran.core.rule.MultipleTransletRule;
import com.aspectran.core.rule.MultipleTransletRuleMap;
import com.aspectran.core.rule.TransletRule;
import com.aspectran.core.rule.TransletRuleMap;

public class TransletRegistry {

	private TransletRuleMap transletRuleMap;
	
	/** The multi activity translet rule map. */
	private MultipleTransletRuleMap multiActivityTransletRuleMap;

	public TransletRegistry(TransletRuleMap transletRuleMap, MultipleTransletRuleMap multiActivityTransletRuleMap) {
		this.transletRuleMap = transletRuleMap;
		this.multiActivityTransletRuleMap = multiActivityTransletRuleMap;
	}
	
	/**
	 * Gets the translet rule map.
	 * 
	 * @return the translet rule map
	 */
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}

	/**
	 * Adds the translet rule.
	 * 
	 * @param transletRule the translet rule
	 * 
	 * @return the translet rule
	 */
	public TransletRule putTransletRule(TransletRule transletRule) {
		return transletRuleMap.put(transletRule.getName(), transletRule);
	}

	/**
	 * Put multi activity translet rule.
	 *
	 * @param name the path
	 * @param responseId the response id
	 * @param transletRule the translet rule
	 * @return the translet rule
	 */
	public TransletRule putMultiActivityTransletRule(String name, String responseId, TransletRule transletRule) {
		MultipleTransletRule matr = new MultipleTransletRule();
		matr.setName(name);
		matr.setResponseId(responseId);
		matr.setTransletRule(transletRule);
		
		multiActivityTransletRuleMap.put(name, matr);
		transletRule.addMultiActivityTransletRule(matr);

		return transletRule;
	}

	public TransletRule getTransletRule(String transletName) {
		return transletRuleMap.get(transletName);
	}
	
	public MultipleTransletRule getMultipleTransletRule(String transletName) {
		return multiActivityTransletRuleMap.get(transletName);
	}
	
	public boolean isMultiActivityEnable() {
		return (multiActivityTransletRuleMap != null && multiActivityTransletRuleMap.size() > 0);
	}
	
	public void destroy() {
		if(transletRuleMap != null) {
			transletRuleMap.clear();
			transletRuleMap = null;
		}
	}

}
