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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class ResponseByContentTypeParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine exceptionType;
	public static final ParameterDefine transforms;
	public static final ParameterDefine dispatchs;
	public static final ParameterDefine forwards;
	public static final ParameterDefine redirects;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		exceptionType = new ParameterDefine("exceptionType", ParameterValueType.STRING);
		transforms = new ParameterDefine("transform", TransformParameters.class, true, true);
		dispatchs = new ParameterDefine("dispatch", DispatchParameters.class, true, true);
		forwards = new ParameterDefine("forward", ForwardParameters.class, true, true);
		redirects = new ParameterDefine("redirect", RedirectParameters.class, true, true);

		parameterDefines = new ParameterDefine[] {
				exceptionType,
				transforms,
				dispatchs,
				forwards,
				redirects
		};
	}
	
	public ResponseByContentTypeParameters() {
		super(parameterDefines);
	}
	
	public ResponseByContentTypeParameters(String text) {
		super(parameterDefines, text);
	}
	
}
