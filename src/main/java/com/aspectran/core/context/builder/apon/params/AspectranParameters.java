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

public class AspectranParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine description;
	public static final ParameterDefine settings;
	public static final ParameterDefine typeAlias;
	public static final ParameterDefine aspects;
	public static final ParameterDefine beans;
	public static final ParameterDefine translets;
	public static final ParameterDefine templates;
	public static final ParameterDefine imports;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		description = new ParameterDefine("description", ParameterValueType.TEXT);
		settings = new ParameterDefine("settings", DefaultSettingsParameters.class);
		typeAlias = new ParameterDefine("typeAlias", GenericParameters.class);
		aspects = new ParameterDefine("aspect", AspectParameters.class, true, true);
		beans = new ParameterDefine("bean", BeanParameters.class, true, true);
		translets = new ParameterDefine("translet", TransletParameters.class, true, true);
		templates = new ParameterDefine("template", TemplateParameters.class, true, true);
		imports = new ParameterDefine("import", ImportParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				description,
				settings,
				typeAlias,
				aspects,
				beans,
				translets,
				templates,
				imports
			};
	}
	
	public AspectranParameters() {
		super(parameterDefines);
	}
	
	public AspectranParameters(String text) {
		super(parameterDefines, text);
	}
	
}
