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

public class TransformParameters extends AbstractParameters {

	public static final ParameterDefine actions;
	public static final ParameterDefine type;
	public static final ParameterDefine contentType;
	public static final ParameterDefine template;
	public static final ParameterDefine characterEncoding;
	public static final ParameterDefine defaultResponse;
	public static final ParameterDefine pretty;
	public static final ParameterDefine builtin;

	private static final ParameterDefine[] parameterDefines;
	
	static {
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		type = new ParameterDefine("type", ParameterValueType.STRING);
		contentType = new ParameterDefine("contentType", ParameterValueType.STRING);
		template = new ParameterDefine("template", ParameterValueType.STRING);
		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
		defaultResponse = new ParameterDefine("defaultResponse", ParameterValueType.BOOLEAN);
		pretty = new ParameterDefine("pretty", ParameterValueType.BOOLEAN);
		builtin = new ParameterDefine("builtin", TemplateParameters.class);

		parameterDefines = new ParameterDefine[] {
				actions,
				type,
				contentType,
				template,
				characterEncoding,
				defaultResponse,
				pretty,
				builtin
		};
	}
	
	public TransformParameters() {
		super(parameterDefines);
	}
	
	public TransformParameters(String text) {
		super(parameterDefines, text);
	}
	
}
