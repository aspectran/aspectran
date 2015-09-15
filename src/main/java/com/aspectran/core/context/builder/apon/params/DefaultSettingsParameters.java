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

public class DefaultSettingsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transletNamePattern;
	public static final ParameterDefine transletNamePatternPrefix;
	public static final ParameterDefine transletNamePatternSuffix;
	public static final ParameterDefine transletInterfaceClass;
	public static final ParameterDefine transletImplementClass;
	public static final ParameterDefine activityDefaultHandler;
	public static final ParameterDefine nullableContentId;
	public static final ParameterDefine nullableActionId;
	public static final ParameterDefine beanProxyMode;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		transletNamePattern = new ParameterDefine("transletNamePattern", ParameterValueType.STRING);
		transletNamePatternPrefix = new ParameterDefine("transletNamePatternPrefix", ParameterValueType.STRING);
		transletNamePatternSuffix = new ParameterDefine("transletNamePatternSuffix", ParameterValueType.STRING);
		transletInterfaceClass = new ParameterDefine("transletInterfaceClass", ParameterValueType.STRING);
		transletImplementClass = new ParameterDefine("transletImplementClass", ParameterValueType.STRING);
		activityDefaultHandler = new ParameterDefine("activityDefaultHandler", ParameterValueType.STRING);
		nullableContentId = new ParameterDefine("nullableContentId", ParameterValueType.BOOLEAN);
		nullableActionId = new ParameterDefine("nullableActionId", ParameterValueType.BOOLEAN);
		beanProxyMode = new ParameterDefine("beanProxyMode", ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				transletNamePattern,
				transletNamePatternPrefix,
				transletNamePatternSuffix,
				transletInterfaceClass,
				transletImplementClass,
				activityDefaultHandler,
				nullableContentId,
				nullableActionId,
				beanProxyMode
		};
	}
	
	public DefaultSettingsParameters() {
		super(parameterDefines);
	}
	
	public DefaultSettingsParameters(String text) {
		super(parameterDefines, text);
	}
	
}
