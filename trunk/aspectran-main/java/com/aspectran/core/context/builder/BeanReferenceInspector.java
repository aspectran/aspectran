package com.aspectran.core.context.builder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class BeanReferenceInspector {

	private Map<String, Set<Object>> relationMap = new LinkedHashMap<String, Set<Object>>();
	
	public BeanReferenceInspector() {
	}
	
	public void putRelation(String beanId, Object rule) {
		Set<Object> ruleSet = relationMap.get(beanId);
		
		if(ruleSet == null) {
			ruleSet = new LinkedHashSet<Object>();
			ruleSet.add(rule);
			relationMap.put(beanId,  ruleSet);
		} else {
			ruleSet.add(rule);
		}
	}
	
	public Map<String, Set<Object>> getRelationMap() {
		return relationMap;
	}
	
}
