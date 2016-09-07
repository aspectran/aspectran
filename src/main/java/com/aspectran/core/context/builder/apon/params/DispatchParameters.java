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

public class DispatchParameters extends AbstractParameters {

	public static final ParameterDefinition actions;
	public static final ParameterDefinition name;
	public static final ParameterDefinition dispatcher;
	public static final ParameterDefinition contentType;
	public static final ParameterDefinition characterEncoding;
	public static final ParameterDefinition defaultResponse;

	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		actions = new ParameterDefinition("action", ActionParameters.class, true, true);
		name = new ParameterDefinition("name", ParameterValueType.STRING);
		dispatcher = new ParameterDefinition("dispatcher", ParameterValueType.STRING);
		contentType = new ParameterDefinition("contentType", ParameterValueType.STRING);
		characterEncoding = new ParameterDefinition("characterEncoding", ParameterValueType.STRING);
		defaultResponse = new ParameterDefinition("defaultResponse", ParameterValueType.BOOLEAN);

		parameterDefinitions = new ParameterDefinition[] {
			actions,
			name,
			dispatcher,
			contentType,
			characterEncoding,
			defaultResponse
		};
	}
	
	public DispatchParameters() {
		super(parameterDefinitions);
	}
	
	public DispatchParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
