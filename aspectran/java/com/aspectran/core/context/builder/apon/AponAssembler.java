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
package com.aspectran.core.context.builder.apon;

import java.util.Iterator;
import java.util.List;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.apon.params.AspectParameters;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.type.DefaultSettingType;

/**
 * AponAspectranContextBuilder.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class AponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	public AponAssembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public void assembleAspectran(Parameters parameters) {
		assembleDefaultSettings(parameters.getParameters(AspectranParameters.setting));
		assembleTypeAlias(parameters.getParameters(AspectranParameters.typeAlias));
		assembleAspects(parameters.getParametersList(AspectranParameters.aspects));
	}
	
	public void assembleDefaultSettings(Parameters parameters) {
		if(parameters == null)
			return;
		
		Iterator<String> iter = parameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String name = iter.next();
			
			DefaultSettingType settingType = null;
			
			if(name != null) {
				settingType = DefaultSettingType.valueOf(name);
				
				if(settingType == null)
					throw new IllegalArgumentException("Unknown setting name '" + name + "'");
			}
			
			assistant.putSetting(settingType, parameters.getString(name));
		}
	}
	
	public void assembleTypeAlias(Parameters parameters) {
		if(parameters == null)
			return;
		
		Iterator<String> iter = parameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String alias = iter.next();
			assistant.addTypeAlias(alias, parameters.getString(alias));
		}
	}
	
	public void assembleAspects(List<Parameters> parametersList) {
		for(Parameters parameters : parametersList) {
			assembleAspect(parameters);
		}
	}
	
	public void assembleAspect(Parameters parameters) {
//		public static final ParameterDefine id;
//		public static final ParameterDefine useFor;
//		public static final ParameterDefine jointpoint;
//		public static final ParameterDefine setting;
//		public static final ParameterDefine advice;

		String id = parameters.getString(AspectParameters.id);
		String useFor = parameters.getString(AspectParameters.useFor);
		Parameters joinpointParams = parameters.getParameters(AspectParameters.jointpoint);
		Parameters settingParams = parameters.getParameters(AspectParameters.setting);
		Parameters asviceParams = parameters.getParameters(AspectParameters.advice);

		AspectRule aspectRule = new AspectRule();
		
	}
	
}
