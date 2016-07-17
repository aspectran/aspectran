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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class JoinpointParameters extends AbstractParameters {

	public static final ParameterDefinition scope;
	public static final ParameterDefinition method;
	public static final ParameterDefinition pointcut;
	
	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		scope = new ParameterDefinition("scope", ParameterValueType.STRING);
		method = new ParameterDefinition("method", ParameterValueType.STRING);
		pointcut = new ParameterDefinition("pointcut", PointcutParameters.class);
		
		parameterDefinitions = new ParameterDefinition[] {
			scope,
			method,
			pointcut
		};
	}
	
	public JoinpointParameters() {
		super(parameterDefinitions);
	}
	
	public JoinpointParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
