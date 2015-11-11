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

public class ReferenceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine parameter;
	public static final ParameterDefine attribute;
	public static final ParameterDefine property;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		parameter = new ParameterDefine("parameter", ParameterValueType.STRING);
		attribute = new ParameterDefine("attribute", ParameterValueType.STRING);
		property = new ParameterDefine("property", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				parameter,
				attribute,
				property
		};
	}
	
	public ReferenceParameters() {
		super(parameterDefines);
	}
	
	public ReferenceParameters(String text) {
		super(parameterDefines, text);
	}
	
}
