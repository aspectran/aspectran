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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.PropertyPossessable;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class EnvironmentRule.
 * 
 * <p>Created: 2016. 05. 06 PM 11:23:35</p>
 */
public class EnvironmentRule implements PropertyPossessable {

	private String profile;
	
	private ItemRuleMap propertyItemRuleMap;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public ItemRuleMap getPropertyItemRuleMap() {
		return propertyItemRuleMap;
	}

	@Override
	public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
		this.propertyItemRuleMap = propertyItemRuleMap;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("profile", profile);
		if (propertyItemRuleMap != null) {
			tsb.append("properties", propertyItemRuleMap.keySet());
		}
		return tsb.toString();
	}
	
	/**
	 * Returns a new instance of EnvironmentRule.
	 *
	 * @param profile the profile
	 * @param propertyItemRuleMap the property item rule map
	 * @return an instance of EnvironmentRule
	 */
	public static EnvironmentRule newInstance(String profile, ItemRuleMap propertyItemRuleMap) {
		EnvironmentRule environmentRule = new EnvironmentRule();
		environmentRule.setProfile(profile);
		environmentRule.setPropertyItemRuleMap(propertyItemRuleMap);
		return environmentRule;
	}
	
}
