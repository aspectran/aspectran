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

public class TemplateParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine file;
	public static final ParameterDefine resource;
	public static final ParameterDefine url;
	public static final ParameterDefine content;
	public static final ParameterDefine encoding;
	public static final ParameterDefine noCache;

	private static final ParameterDefine[] parameterDefines;

	static {
		file = new ParameterDefine("file", ParameterValueType.STRING);
		resource = new ParameterDefine("resource", ParameterValueType.STRING);
		url = new ParameterDefine("url", ParameterValueType.STRING);
		content = new ParameterDefine("content", ParameterValueType.TEXT);
		encoding = new ParameterDefine("encoding", ParameterValueType.STRING);
		noCache = new ParameterDefine("noCache", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				file,
				resource,
				url,
				content,
				encoding,
				noCache
		};
	}
	
	public TemplateParameters() {
		super(parameterDefines);
	}
	
	public TemplateParameters(String text) {
		super(parameterDefines, text);
	}
	
}
