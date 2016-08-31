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
package com.aspectran.core.context.schedule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ScheduleRuleRegistry.
 */
public class ScheduleRuleRegistry {

	private final Log log = LogFactory.getLog(ScheduleRuleRegistry.class);

	private final Map<String, ScheduleRule> scheduleRuleMap = new LinkedHashMap<>();
	
	public ScheduleRuleRegistry() {
	}
	
	public Map<String, ScheduleRule> getScheduleRuleMap() {
		return scheduleRuleMap;
	}

	public boolean contains(String scheduleId) {
		return scheduleRuleMap.containsKey(scheduleId);
	}
	
	public ScheduleRule getScheduleRule(String scheduleId) {
		return scheduleRuleMap.get(scheduleId);
	}

	public void addScheduleRule(ScheduleRule scheduleRule) {
		scheduleRuleMap.put(scheduleRule.getId(), scheduleRule);
		
		if(log.isDebugEnabled())
			log.debug("add ScheduleRule " + scheduleRule);
	}

	public Collection<ScheduleRule> getScheduleRules() {
		return scheduleRuleMap.values();
	}

	public void clear() {
		scheduleRuleMap.clear();
	}

}
