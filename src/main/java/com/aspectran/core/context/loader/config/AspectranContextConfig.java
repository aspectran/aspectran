/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AspectranContextConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine root;
	public static final ParameterDefine encoding;
	public static final ParameterDefine resources;
	public static final ParameterDefine hybridLoading;
	public static final ParameterDefine autoReloading;
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		root = new ParameterDefine("root", ParameterValueType.STRING);
		encoding = new ParameterDefine("encoding", ParameterValueType.STRING);
		resources = new ParameterDefine("resources", ParameterValueType.STRING, true);
		hybridLoading = new ParameterDefine("hybridLoading", ParameterValueType.BOOLEAN);
		autoReloading = new ParameterDefine("autoReloading", AspectranContextAutoReloadingConfig.class);
		
		parameterDefines = new ParameterDefine[] {
				root,
				encoding,
				resources,
				hybridLoading,
				autoReloading
		};
	}
	
	public AspectranContextConfig() {
		super(parameterDefines);
	}
	
	public AspectranContextConfig(String text) {
		super(parameterDefines, text);
	}
	
}
