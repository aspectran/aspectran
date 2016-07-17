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

public class ResponseParameters extends AbstractParameters {

	public static final ParameterDefinition name;
	public static final ParameterDefinition characterEncoding;
	public static final ParameterDefinition transform;
	public static final ParameterDefinition dispatch;
	public static final ParameterDefinition forward;
	public static final ParameterDefinition redirect;
	
	private static final ParameterDefinition[] parameterDefinitions;

	static {
		name = new ParameterDefinition("name", ParameterValueType.STRING);
		characterEncoding = new ParameterDefinition("characterEncoding", ParameterValueType.STRING);
		transform = new ParameterDefinition("transform", TransformParameters.class);
		dispatch = new ParameterDefinition("dispatch", DispatchParameters.class);
		forward = new ParameterDefinition("forward", ForwardParameters.class);
		redirect = new ParameterDefinition("redirect", RedirectParameters.class);
	
		parameterDefinitions = new ParameterDefinition[] {
			name,
			characterEncoding,
			transform,
			dispatch,
			forward,
			redirect
		};
	}
	
	public ResponseParameters() {
		super(parameterDefinitions);
	}
	
	public ResponseParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
