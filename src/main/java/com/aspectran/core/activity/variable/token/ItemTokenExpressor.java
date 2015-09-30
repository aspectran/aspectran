/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity.variable.token;

import com.aspectran.core.activity.variable.ValueMap;
import com.aspectran.core.context.rule.ItemRuleMap;

/**
 * The Interface ItemTokenExpressor.
 *
 * @since 2010. 5. 6
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
	public void express(ItemRuleMap itemRuleMap, ValueMap valueMap);
	
	/**
	 * Sets the token value handler.
	 *
	 * @param tokenValueHandler the new token value handler
	 */
	//public void setTokenValueHandler(TokenValueHandler tokenValueHandler);

}
