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
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class BeanReferenceInspector.
 */
public class BeanReferenceInspector {

	private final Log log = LogFactory.getLog(BeanReferenceInspector.class);
	
	private final Map<Object, Set<BeanReferenceInspectable>> relationMap = new LinkedHashMap<Object, Set<BeanReferenceInspectable>>();
	
	public BeanReferenceInspector() {
	}
	
	public void putRelation(Object beanIdOrClass, BeanReferenceInspectable someRule) {
		Set<BeanReferenceInspectable> ruleSet = relationMap.get(beanIdOrClass);
		
		if(ruleSet == null) {
			ruleSet = new LinkedHashSet<BeanReferenceInspectable>();
			ruleSet.add(someRule);
			relationMap.put(beanIdOrClass, ruleSet);
		} else {
			ruleSet.add(someRule);
		}
	}
	
	public void inspect(BeanRuleRegistry beanRuleRegistry) throws BeanReferenceException {
		List<Object> unknownBeanIdList = new ArrayList<Object>();
		
		for(Map.Entry<Object, Set<BeanReferenceInspectable>> entry : relationMap.entrySet()) {
			Object beanIdOrClass = entry.getKey();
			Set<BeanReferenceInspectable> set = entry.getValue();

			BeanRule beanRule = beanRuleRegistry.getBeanRule(beanIdOrClass);
			
			if(beanRule == null) {
				unknownBeanIdList.add(beanIdOrClass);

				for(BeanReferenceInspectable o : set) {
					log.error("Cannot resolve reference to bean '" + beanIdOrClass.toString() + "' on " + o.getBeanReferrerType() + " " + o);
				}
			} else {
				for(BeanReferenceInspectable o : set) {
					if(o.getBeanReferrerType() == BeanReferrerType.BEAN_ACTION_RULE) {
						BeanActionRule.checkTransletActionParameter((BeanActionRule)o, beanRule);
					} else if(o.getBeanReferrerType() == BeanReferrerType.TOKEN) {
						Token token = (Token)o;
						String propertyName = token.getPropertyName();
						Class<?> beanClass = beanRule.getTargetBeanClass();
//						BeanDescriptor bd = BeanDescriptor.getInstance(beanClass).getGetter(propertyName);
//
//
//								for(Token token : iter.next()) {
//									if(token.getType() == TokenType.BEAN) {
//										String getterName = token.getPropertyName();
//										//BeanDescriptor.getInstance()
//
//										//BeanUtils.getClassPropertyTypeForGetter(token.getBeanClass(), getterName);
//									}
//								}
//							}
//						}
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
	
	public Map<Object, Set<BeanReferenceInspectable>> getRelationMap() {
		return relationMap;
	}
	
}
