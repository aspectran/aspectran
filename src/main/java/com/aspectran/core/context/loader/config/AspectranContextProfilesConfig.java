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
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;

public class AspectranContextProfilesConfig extends AbstractParameters {

	public static final ParameterDefine activeProfiles;
	public static final ParameterDefine defaultProfiles;
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		activeProfiles = new ParameterDefine("active", ParameterValueType.STRING, true);
		defaultProfiles = new ParameterDefine("default", ParameterValueType.STRING, true);
		
		parameterDefines = new ParameterDefine[] {
				activeProfiles,
				defaultProfiles
		};
	}
	
	public AspectranContextProfilesConfig() {
		super(parameterDefines);
	}
	
	public AspectranContextProfilesConfig(String text) {
		super(parameterDefines, text);
	}
	
}
