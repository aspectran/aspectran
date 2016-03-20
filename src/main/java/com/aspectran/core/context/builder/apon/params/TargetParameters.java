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
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class TargetParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine pluses;
	public static final ParameterDefine minuses;
	
	public static final ParameterDefine translet;
	public static final ParameterDefine bean;
	public static final ParameterDefine method;
	public static final ParameterDefine excludeTargets;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		pluses = new ParameterDefine("+", ParameterValueType.STRING, true, true);
		minuses = new ParameterDefine("-", ParameterValueType.STRING, true, true);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		method = new ParameterDefine("method", ParameterValueType.STRING);
		excludeTargets = new ParameterDefine("exclude", ExcludeTargetParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				pluses,
				minuses,
				translet,
				bean,
				method,
				excludeTargets
		};
	}
	
	public TargetParameters() {
		super(parameterDefines);
	}
	
	public TargetParameters(String text) {
		super(parameterDefines, text);
	}
	
}
