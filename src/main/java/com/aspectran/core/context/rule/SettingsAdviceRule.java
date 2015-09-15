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
package com.aspectran.core.context.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;

public class SettingsAdviceRule {

	private String aspectId;
	
	private AspectAdviceType aspectAdviceType;

	private Map<String, String> settings;

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

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}
	
	public String getSetting(String name) {
		return settings.get(name);
	}
	
	public void putSetting(String name, String value) {
		if(settings == null) {
			settings = new HashMap<String, String>();
		}
		
		settings.put(name, value);
	}
	
	public static SettingsAdviceRule newInstance(AspectRule aspectRule, String text) {
		if(StringUtils.hasText(text)) {
			Parameters settingsParameters = new GenericParameters(text);
			return newInstance(aspectRule, settingsParameters);
		} else {
			return newInstance(aspectRule, (Parameters)null);
		}
	}
	
	public static SettingsAdviceRule newInstance(AspectRule aspectRule, Parameters settingsParameters) {
		SettingsAdviceRule sar = new SettingsAdviceRule();
		sar.setAspectId(aspectRule.getId());
		sar.setAspectAdviceType(AspectAdviceType.SETTINGS);
	
		if(settingsParameters != null) {
			Set<String> parametersNames = settingsParameters.getParameterNameSet();
			
			if(parametersNames != null) {
				for(String name : parametersNames) {
					sar.putSetting(name, settingsParameters.getString(name));
				}
			}
		}

		return sar;
	}

}
