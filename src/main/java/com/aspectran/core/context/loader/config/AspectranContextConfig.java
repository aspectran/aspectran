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
package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class AspectranContextConfig extends AbstractParameters {

	public static final ParameterDefinition root;
	public static final ParameterDefinition encoding;
	public static final ParameterDefinition resources;
	public static final ParameterDefinition hybridLoad;
	public static final ParameterDefinition autoReload;
	public static final ParameterDefinition profiles;
	
	private final static ParameterDefinition[] parameterDefinitions;
	
	static {
		root = new ParameterDefinition("root", ParameterValueType.STRING);
		encoding = new ParameterDefinition("encoding", ParameterValueType.STRING);
		resources = new ParameterDefinition("resources", ParameterValueType.STRING, true);
		hybridLoad = new ParameterDefinition("hybridLoad", ParameterValueType.BOOLEAN);
		autoReload = new ParameterDefinition("autoReload", AspectranContextAutoReloadConfig.class);
		profiles = new ParameterDefinition("profiles", AspectranContextProfilesConfig.class);
		
		parameterDefinitions = new ParameterDefinition[] {
			root,
			encoding,
			resources,
			hybridLoad,
			autoReload,
			profiles
		};
	}
	
	public AspectranContextConfig() {
		super(parameterDefinitions);
	}
	
	public AspectranContextConfig(String text) {
		super(parameterDefinitions, text);
	}
	
}
