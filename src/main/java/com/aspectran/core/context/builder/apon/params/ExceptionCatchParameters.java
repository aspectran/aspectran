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

public class ExceptionCatchParameters extends AbstractParameters {

	public static final ParameterDefinition exception;
	public static final ParameterDefinition transforms;
	public static final ParameterDefinition dispatchs;
	public static final ParameterDefinition forwards;
	public static final ParameterDefinition redirects;
	
	private static final ParameterDefinition[] parameterDefinitions;

	static {
		exception = new ParameterDefinition("exception", ParameterValueType.STRING);
		transforms = new ParameterDefinition("transform", TransformParameters.class, true, true);
		dispatchs = new ParameterDefinition("dispatch", DispatchParameters.class, true, true);
		forwards = new ParameterDefinition("forward", ForwardParameters.class, true, true);
		redirects = new ParameterDefinition("redirect", RedirectParameters.class, true, true);

		parameterDefinitions = new ParameterDefinition[] {
			exception,
			transforms,
			dispatchs,
			forwards,
			redirects
		};
	}
	
	public ExceptionCatchParameters() {
		super(parameterDefinitions);
	}
	
	public ExceptionCatchParameters(String text) {
		super(parameterDefinitions, text);
	}
	
}
