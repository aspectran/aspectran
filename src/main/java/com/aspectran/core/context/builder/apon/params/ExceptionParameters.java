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

public class ExceptionParameters extends AbstractParameters {

	public static final ParameterDefine description;
	public static final ParameterDefine action;
	public static final ParameterDefine responseByContentTypes;

	private static final ParameterDefine[] parameterDefines;

	static {
		description = new ParameterDefine("description", ParameterValueType.TEXT);
		action = new ParameterDefine("action", ActionParameters.class);
		responseByContentTypes = new ParameterDefine("responseByContentType", ResponseByContentTypeParameters.class, true, true);

		parameterDefines = new ParameterDefine[] {
				description,
				action,
				responseByContentTypes
		};
	}

	public ExceptionParameters() {
		super(parameterDefines);
	}

	public ExceptionParameters(String text) {
		super(parameterDefines, text);
	}
	
}
