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

public class RedirectParameters extends AbstractParameters {

	public static final ParameterDefinition actions;
	public static final ParameterDefinition contentType;
	public static final ParameterDefinition target;
	public static final ParameterDefinition excludeNullParameter;
	public static final ParameterDefinition defaultResponse;
	public static final ParameterDefinition parameters;

	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		actions = new ParameterDefinition("action", ActionParameters.class, true, true);
		contentType = new ParameterDefinition("contentType", ParameterValueType.STRING);
		target = new ParameterDefinition("target", ParameterValueType.STRING);
		excludeNullParameter = new ParameterDefinition("excludeNullParameter", ParameterValueType.BOOLEAN);
		defaultResponse = new ParameterDefinition("defaultResponse", ParameterValueType.BOOLEAN);
		parameters = new ParameterDefinition("parameter", ItemHolderParameters.class);

		parameterDefinitions = new ParameterDefinition[] {
			actions,
			contentType,
			target,
			excludeNullParameter,
			defaultResponse,
			parameters
		};
	}
	
	public RedirectParameters() {
		super(parameterDefinitions);
	}
	
	public RedirectParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
