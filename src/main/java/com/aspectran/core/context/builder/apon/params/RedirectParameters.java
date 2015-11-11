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

public class RedirectParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine contentType;
	public static final ParameterDefine translet;
	public static final ParameterDefine url;
	public static final ParameterDefine parameters;
	public static final ParameterDefine excludeNullParameter;
	public static final ParameterDefine actions;
	public static final ParameterDefine defaultResponse;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		translet = new ParameterDefine("translet", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		parameters = new ParameterDefine("parameter", ItemHolderParameters.class);
		excludeNullParameter = new ParameterDefine("excludeNullParameter", ParameterValueType.BOOLEAN);
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		defaultResponse = new ParameterDefine("defaultResponse", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				contentType,
				translet,
				url,
				parameters,
				excludeNullParameter,
				actions,
				defaultResponse
		};
	}
	
	public RedirectParameters() {
		super(parameterDefines);
	}
	
	public RedirectParameters(String text) {
		super(parameterDefines, text);
	}
	
}
