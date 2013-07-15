/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.token.expression;

import java.io.File;
import java.util.Map;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.context.bean.registry.BeanRegistry;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.token.Token;
import com.aspectran.core.type.ItemType;
import com.aspectran.core.type.ItemValueType;
import com.aspectran.core.variable.ValueMap;

/**
 * <p>Created: 2008. 06. 19 오후 9:43:28</p>
 */
public class ItemTokenExpression extends TokenExpression implements ItemTokenExpressor {

	/**
	 * Instantiates a new value expression.
	 *
	 * @param beanRegistry the bean registry
	 */
	public ItemTokenExpression(BeanRegistry beanRegistry) {
		super(beanRegistry);
	}
	
	/**
	 * Instantiates a new value expression.
	 *
	 * @param activity the activity
	 */
	public ItemTokenExpression(AspectranActivity activity) {
		super(activity);
	}
	
	/**
	 * Express.
	 * 
	 * @param itemRuleMap the value rule map
	 * 
	 * @return the value map
	 */
	public ValueMap express(ItemRuleMap itemRuleMap) {
		ValueMap valueMap = new ValueMap();
		
		express(itemRuleMap, valueMap);
		
		return valueMap;
	}
	
	/**
	 * Express.
	 * 
	 * @param itemRuleMap the item rule map
	 * @param valueMap the value map
	 * 
	 * @return the value map
	 */
	public void express(ItemRuleMap itemRuleMap, Map<String, Object> valueMap) {
		express(this, itemRuleMap, valueMap);
	}
	
	/**
	 * Express.
	 *
	 * @param expressor the expressor
	 * @param itemRuleMap the item rule map
	 * @param valueMap the value map
	 */
	public void express(TokenExpressor expressor, ItemRuleMap itemRuleMap, Map<String, Object> valueMap) {
		for(ItemRule ir : itemRuleMap) {
			ItemType itemType = ir.getType();
			ItemValueType valueType = ir.getValueType();
			Object value = null;
			
			if(itemType == ItemType.ITEM) {
				Token[] tokens = ir.getTokens();
				
				if(tokens == null || tokens.length == 0)
					value = expressor.expressAsObject(ir.getName(), null);
				else if(tokens.length == 1)
					value = expressor.expressAsObject(ir.getName(), tokens[0]);
				else
					value = expressor.expressAsString(ir.getName(), tokens);

				
				//TODO
				if(value != null) {
					if(valueType == ItemValueType.STRING)
						value = value.toString();
					else if(valueType == ItemValueType.INT)
						value = Integer.parseInt(value.toString());
					else if(valueType == ItemValueType.FILE) {
						String filePathName = value.toString();
						value = new File(filePathName);
					}
				}

			} else if(itemType == ItemType.LIST) {
				value = expressor.expressAsList(ir.getName(), ir.getTokensList());
			} else if(itemType == ItemType.MAP) {
				value = expressor.expressAsMap(ir.getName(), ir.getTokensMap());
			}
			
			valueMap.put(ir.getName(), value);
		}
	}

//	
//	/**
//	 * Express as parameter map.
//	 * 
//	 * @param parameterRuleMap the parameter rule map
//	 * 
//	 * @return the parameter map
//	 */
//	public ParameterMap expressAsParameterMap(ParameterRuleMap parameterRuleMap) {
//		if(parameterRuleMap == null || parameterRuleMap.size() == 0)
//			return null;
//		
//		ParameterMap parameterMap = new ParameterMap();
//		
//		for(ParameterRule pr : parameterRuleMap) {
//			String name = pr.getName();
//			ParameterUnityType paramType = pr.getUnityType();
//			
//			if(paramType == ParameterUnityType.UNITY) {
//				Token[] tokens = pr.getTokens();
//				String value = null;
//
//				if(tokens == null || tokens.length == 0) {
//					value = expressAsString(pr.getName(), null);
//				} else if(tokens.length == 1) {
//					Object obj = expressAsObject(pr.getName(), tokens[0]);
//
//					if(obj != null)
//						value = obj.toString();
//				} else {
//					value = expressAsString(pr.getName(), tokens);
//				}
//
//				parameterMap.put(name, value);
//			} else if(paramType == ParameterUnityType.ARRAY) {
//				Object[] value = expressAsArray(pr.getName(), pr.getTokensArray());
//				parameterMap.put(name, value);
//			} else if(paramType == ParameterUnityType.MAP) {
//				Object value = expressAsMap(pr.getName(), pr.getTokensMap());
//				parameterMap.put(name, value.toString());
//			}
//		}
//		
//		return parameterMap;
//	}
}
