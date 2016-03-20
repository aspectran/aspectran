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
package com.aspectran.core.context.expr;

import java.util.Map;

import com.aspectran.core.context.rule.ItemRuleMap;

/**
 * The Interface ItemTokenExpressor.
 *
 * @since 2010. 5. 6
 */
public interface ItemExpressor {
	
	/**
	 * Expression processing for Item Rule.
	 * 
	 * @param itemRuleMap the item rule map
	 * @return the value map
	 */
	Map<String, Object> express(ItemRuleMap itemRuleMap);
	
	/**
	 * Expression processing for Item Rule.
	 * 
	 * @param itemRuleMap the item rule map
	 * @param valueMap the value map
	 */
	void express(ItemRuleMap itemRuleMap, Map<String, Object> valueMap);
	
}
