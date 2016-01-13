/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.rule.*;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class BeanReferenceInspector.
 */
public class BeanReferenceInspector {

	private final Log log = LogFactory.getLog(BeanReferenceInspector.class);
	
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
	
	public void inspect(BeanRuleRegistry beanRuleRegistry) {
		List<String> unknownBeanIdList = new ArrayList<String>();
		
		for(Map.Entry<String, Set<Object>> entry : relationMap.entrySet()) {
			String beanId = entry.getKey();

			if(!beanRuleRegistry.contains(beanId)) {
				unknownBeanIdList.add(beanId);
				
				Set<Object> set = entry.getValue();
				
				for(Object o : set) {
					String ruleName;
					
					if(o instanceof BeanActionRule) {
						ruleName = "beanActionRule";
					} else if(o instanceof ItemRule) {
						ruleName = "itemRule";
					} else if(o instanceof TransformRule) {
						ruleName = "transformRule";
					} else if(o instanceof RedirectResponseRule) {
						ruleName = "redirectResponseRule";
					} else if(o instanceof TemplateRule) {
						ruleName = "templateRule";
					} else {
						ruleName = "rule";
					}
					
					log.error("Cannot resolve reference to bean '" + beanId + "' on " + ruleName + " " + o);
				}
			}
		}
		
		if(!unknownBeanIdList.isEmpty()) {
			for(String beanId : unknownBeanIdList) {
				relationMap.remove(beanId);
			}
			
			BeanReferenceException bre = new BeanReferenceException(unknownBeanIdList);
			bre.setBeanReferenceInspector(this);
			
			throw bre;
		}
	}
	
	public Map<String, Set<Object>> getRelationMap() {
		return relationMap;
	}
	
}
