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

import java.util.Map;

import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.var.ValueMap;

/**
 *
 * <p>Created: 2010. 5. 6. 오전 1:41:04</p>
 *
 */
public interface ItemTokenExpressor {

	/**
	 * Express.
	 * 
	 * @param itemRuleMap the value rule map
	 * 
	 * @return the value map
	 */
	public ValueMap express(ItemRuleMap itemRuleMap);
	
	/**
	 * Express.
	 * 
	 * @param itemRuleMap the item rule map
	 * @param valueMap the value map
	 * 
	 * @return the value map
	 */
	public void express(ItemRuleMap itemRuleMap, Map<String, Object> valueMap);
	
}
