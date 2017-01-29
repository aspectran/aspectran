/**
 * Copyright 2008-2017 Juho Jeong
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

public class PointcutParameters extends AbstractParameters {

	public static final ParameterDefinition type;
	public static final ParameterDefinition pluses;
	public static final ParameterDefinition minuses;
	public static final ParameterDefinition includes;
	public static final ParameterDefinition execludes;
	
	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		type = new ParameterDefinition("type", ParameterValueType.STRING);
		pluses = new ParameterDefinition("+", ParameterValueType.STRING, true, true);
		minuses = new ParameterDefinition("-", ParameterValueType.STRING, true, true);
		includes = new ParameterDefinition("include", PointcutTargetParameters.class, true, true);
		execludes = new ParameterDefinition("execlude", PointcutTargetParameters.class, true, true);
	
		parameterDefinitions = new ParameterDefinition[] {
			type,
			pluses,
			minuses,
			includes,
			execludes
		};
	}
	
	public PointcutParameters() {
		super(parameterDefinitions);
	}
	
	public PointcutParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
