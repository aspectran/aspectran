/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.base.rule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Created: 2008. 03. 22 오후 5:51:58
 * </p>
 */
public class DispatcherViewsRule {
	
	/** The default dispatch view id. */
	private String defaultDispatcherViewTypeId;
	
	private DispatcherViewTypeRule defaultDispatcherViewTypeRule;

	private Map<String, DispatcherViewTypeRule> dispatcherViewTypeRuleMap;
	
	public String getDefaultDispatcherViewTypeId() {
		return defaultDispatcherViewTypeId;
	}

	public void setDefaultDispatcherViewTypeId(String defaultDispatcherViewTypeId) {
		if(defaultDispatcherViewTypeId == null) {
			this.defaultDispatcherViewTypeId = null;
			defaultDispatcherViewTypeRule = null;
		} else {
			this.defaultDispatcherViewTypeId = defaultDispatcherViewTypeId;
		}
		
		if(dispatcherViewTypeRuleMap != null && dispatcherViewTypeRuleMap.size() > 0) {
			DispatcherViewTypeRule dispatchViewType = dispatcherViewTypeRuleMap.get(defaultDispatcherViewTypeId);
			
			if(dispatchViewType != null) {
				defaultDispatcherViewTypeId = dispatchViewType.getId();
				defaultDispatcherViewTypeRule = dispatchViewType;
			}
		}
	}

	public void addDispatcherViewTypeRule(DispatcherViewTypeRule dispatcherViewTypeRule) {
		if(dispatcherViewTypeRuleMap == null) {
			dispatcherViewTypeRuleMap = new LinkedHashMap<String, DispatcherViewTypeRule>();
			
			if(defaultDispatcherViewTypeId != null) {
				if(defaultDispatcherViewTypeId.equals(dispatcherViewTypeRule.getId()))
					defaultDispatcherViewTypeRule = dispatcherViewTypeRule;
			} else {
				defaultDispatcherViewTypeRule = dispatcherViewTypeRule;
			}

			dispatcherViewTypeRuleMap.put(dispatcherViewTypeRule.getId(), dispatcherViewTypeRule);
		} else {
			dispatcherViewTypeRuleMap.put(dispatcherViewTypeRule.getId(), dispatcherViewTypeRule);

			if(defaultDispatcherViewTypeId != null && defaultDispatcherViewTypeId.equals(dispatcherViewTypeRule.getId())) {
				defaultDispatcherViewTypeRule = dispatcherViewTypeRule;
			}
		}
	}
	
	public DispatcherViewTypeRule getDispatcherViewTypeRule() {
		if(defaultDispatcherViewTypeRule != null)
			return defaultDispatcherViewTypeRule;

		if(dispatcherViewTypeRuleMap != null && dispatcherViewTypeRuleMap.size() > 0) {
			if(defaultDispatcherViewTypeId == null) {
				return dispatcherViewTypeRuleMap.values().iterator().next();
			} else {
				return dispatcherViewTypeRuleMap.get(defaultDispatcherViewTypeId);
			}
		}
		
		return null;
	}
	
	public DispatcherViewTypeRule getDispatcherViewTypeRule(String dispatcherViewTypeId) {
		if(dispatcherViewTypeRuleMap != null && dispatcherViewTypeRuleMap.size() > 0) {
			return dispatcherViewTypeRuleMap.get(defaultDispatcherViewTypeId);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{defaultDispatcherViewTypeId=").append(defaultDispatcherViewTypeId);
		
		if(dispatcherViewTypeRuleMap != null) {
			sb.append(", dispatcherViewTypeRuleMap=");
			sb.append(dispatcherViewTypeRuleMap);
		}

		sb.append("}");
		
		return sb.toString();
	}
}
