/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine name;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine transform;
	public static final ParameterDefine dispatch;
	public static final ParameterDefine forward;
	public static final ParameterDefine redirect;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		name = new ParameterDefine("name", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		transform = new ParameterDefine("transform", TransformParameters.class);
		dispatch = new ParameterDefine("dispatch", DispatchParameters.class);
		forward = new ParameterDefine("forward", ForwardParameters.class);
		redirect = new ParameterDefine("redirect", RedirectParameters.class);
	
		parameterDefines = new ParameterDefine[] {
				name,
				characterEncoding,
				transform,
				dispatch,
				forward,
				redirect
		};
	}
	
	public ResponseParameters() {
		super(parameterDefines);
	}
	
	public ResponseParameters(String text) {
		super(parameterDefines, text);
	}
	
}
