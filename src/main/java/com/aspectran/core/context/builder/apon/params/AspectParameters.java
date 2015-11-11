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
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AspectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine description;
	public static final ParameterDefine id;
	public static final ParameterDefine useFor;
	public static final ParameterDefine jointpoint;
	public static final ParameterDefine settings;
	public static final ParameterDefine advice;
	public static final ParameterDefine exceptionRaised;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		description = new ParameterDefine("description", ParameterValueType.TEXT);
		id = new ParameterDefine("id", ParameterValueType.STRING);
		useFor = new ParameterDefine("for", ParameterValueType.STRING);
		jointpoint = new ParameterDefine("joinpoint", JoinpointParameters.class);
		settings = new ParameterDefine("settings", GenericParameters.class);
		advice = new ParameterDefine("advice", AdviceParameters.class);
		exceptionRaised = new ParameterDefine("exceptionRaised", ExceptionRaisedParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				description,
				id,
				useFor,
				jointpoint,
				settings,
				advice,
				exceptionRaised
			};
	}
	
	public AspectParameters() {
		super(parameterDefines);
	}
	
	public AspectParameters(String text) {
		super(parameterDefines, text);
	}
	
}
