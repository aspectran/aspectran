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

public class ActionParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine id;
	
	public static final ParameterDefine beanId;
	public static final ParameterDefine methodName;
	public static final ParameterDefine arguments;
	public static final ParameterDefine properties;
	
	public static final ParameterDefine include;
	public static final ParameterDefine attributes;

	public static final ParameterDefine echo;
	
	public static final ParameterDefine hidden;
	
	private static final ParameterDefine[] parameterDefines;

	static {
		id = new ParameterDefine("id", ParameterValueType.STRING);
		beanId = new ParameterDefine("bean", ParameterValueType.STRING);
		methodName = new ParameterDefine("method", ParameterValueType.STRING);
		arguments = new ParameterDefine("argument", ItemHolderParameters.class);
		properties = new ParameterDefine("property", ItemHolderParameters.class);
		include = new ParameterDefine("include", ParameterValueType.STRING);
		attributes = new ParameterDefine("attribute", ItemHolderParameters.class);
		echo = new ParameterDefine("echo", ItemHolderParameters.class);
		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				id,
				beanId,
				methodName,
				arguments,
				properties,
				include,
				attributes,
				echo,
				hidden
		};
	}
	
	public ActionParameters() {
		super(parameterDefines);
	}
	
	public ActionParameters(String plaintext) {
		super(parameterDefines, plaintext);
	}
	
}
