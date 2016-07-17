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

public class ReferenceParameters extends AbstractParameters {

	public static final ParameterDefinition parameter;
	public static final ParameterDefinition attribute;
	public static final ParameterDefinition bean;
	public static final ParameterDefinition property;
	
	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		parameter = new ParameterDefinition("parameter", ParameterValueType.STRING);
		attribute = new ParameterDefinition("attribute", ParameterValueType.STRING);
		bean = new ParameterDefinition("bean", ParameterValueType.STRING);
		property = new ParameterDefinition("property", ParameterValueType.STRING);
		
		parameterDefinitions = new ParameterDefinition[] {
			parameter,
			attribute,
			bean,
			property
		};
	}
	
	public ReferenceParameters() {
		super(parameterDefinitions);
	}
	
	public ReferenceParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
