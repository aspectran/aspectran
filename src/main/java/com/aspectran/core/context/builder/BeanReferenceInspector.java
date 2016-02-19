/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	
	private Map<Object, Set<Object>> relationMap = new LinkedHashMap<Object, Set<Object>>();
	
	public BeanReferenceInspector() {
	}
	
	public void putRelation(Object beanIdOrClass, Object someRule) {
		Set<Object> ruleSet = relationMap.get(beanIdOrClass);
		
		if(ruleSet == null) {
			ruleSet = new LinkedHashSet<Object>();
			ruleSet.add(someRule);
			relationMap.put(beanIdOrClass, ruleSet);
		} else {
			ruleSet.add(someRule);
		}
	}
	
	public void inspect(BeanRuleRegistry beanRuleRegistry) throws BeanReferenceException {
		List<Object> unknownBeanIdList = new ArrayList<Object>();
		
		for(Map.Entry<Object, Set<Object>> entry : relationMap.entrySet()) {
			Object beanIdOrClass = entry.getKey();
			BeanRule beanRule = beanRuleRegistry.getBeanRule(beanIdOrClass);
			
			if(!beanRuleRegistry.contains(beanIdOrClass)) {
				unknownBeanIdList.add(beanIdOrClass);
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
					
					log.error("Cannot resolve reference to bean '" + beanIdOrClass.toString() + "' on " + ruleName + " " + o);
				}
			} else {
				Set<Object> set = entry.getValue();
				
				for(Object o : set) {
					if(o instanceof BeanActionRule) {
						BeanActionRule.checkActionParameter((BeanActionRule)o, beanRule);
					}
				}
			}
		}
		
		if(!unknownBeanIdList.isEmpty()) {
			for(Object beanIdOrClass : unknownBeanIdList) {
				relationMap.remove(beanIdOrClass);
			}
			
			BeanReferenceException bre = new BeanReferenceException(unknownBeanIdList);
			bre.setBeanReferenceInspector(this);
			
			throw bre;
		}
	}
	
	public Map<Object, Set<Object>> getRelationMap() {
		return relationMap;
	}
	
}
