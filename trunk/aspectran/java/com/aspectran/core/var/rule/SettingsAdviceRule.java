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
package com.aspectran.core.var.rule;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.var.type.AspectAdviceType;

public class SettingsAdviceRule {

	private String aspectId;
	
	private AspectAdviceType aspectAdviceType;

	private Map<String, Object> settings;

	public String getAspectId() {
		return aspectId;
	}

	public void setAspectId(String aspectId) {
		this.aspectId = aspectId;
	}

	public AspectAdviceType getAspectAdviceType() {
		return aspectAdviceType;
	}

	public void setAspectAdviceType(AspectAdviceType aspectAdviceType) {
		this.aspectAdviceType = aspectAdviceType;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, Object> settings) {
		this.settings = settings;
	}
	
	public Object getSetting(String name) {
		return settings.get(name);
	}
	
	public void putSetting(String name, String value) {
		if(settings == null) {
			settings = new HashMap<String, Object>();
		}
		
		settings.put(name, value);
	}
	
	public static SettingsAdviceRule newInstance(AspectRule aspectRule) {
		SettingsAdviceRule sar = new SettingsAdviceRule();
		sar.setAspectId(aspectRule.getId());
		sar.setAspectAdviceType(AspectAdviceType.SETTINGS);

		return sar;
	}

}
