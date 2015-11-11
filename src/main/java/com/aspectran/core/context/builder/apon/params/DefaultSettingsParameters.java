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

import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class DefaultSettingsParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine transletNamePattern;
	public static final ParameterDefine transletNamePrefix;
	public static final ParameterDefine transletNameSuffix;
	public static final ParameterDefine transletInterfaceClass;
	public static final ParameterDefine transletImplementClass;
	public static final ParameterDefine nullableContentId;
	public static final ParameterDefine nullableActionId;
	public static final ParameterDefine beanProxifier;
	public static final ParameterDefine pointcutPatternVerifiable;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		transletNamePattern = new ParameterDefine(DefaultSettingType.TRANSLET_NAME_PATTERN.toString(), ParameterValueType.STRING);
		transletNamePrefix = new ParameterDefine(DefaultSettingType.TRANSLET_NAME_PREFIX.toString(), ParameterValueType.STRING);
		transletNameSuffix = new ParameterDefine(DefaultSettingType.TRANSLET_NAME_SUFFIX.toString(), ParameterValueType.STRING);
		transletInterfaceClass = new ParameterDefine(DefaultSettingType.TRANSLET_INTERFACE_CLASS.toString(), ParameterValueType.STRING);
		transletImplementClass = new ParameterDefine(DefaultSettingType.TRANSLET_IMPLEMENT_CLASS.toString(), ParameterValueType.STRING);
		nullableContentId = new ParameterDefine(DefaultSettingType.NULLABLE_CONTENT_ID.toString(), ParameterValueType.BOOLEAN);
		nullableActionId = new ParameterDefine(DefaultSettingType.NULLABLE_ACTION_ID.toString(), ParameterValueType.BOOLEAN);
		beanProxifier = new ParameterDefine(DefaultSettingType.BEAN_PROXIFIER.toString(), ParameterValueType.STRING);
		pointcutPatternVerifiable = new ParameterDefine(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE.toString(), ParameterValueType.STRING);
		
		parameterDefines = new ParameterDefine[] {
				transletNamePattern,
				transletNamePrefix,
				transletNameSuffix,
				transletInterfaceClass,
				transletImplementClass,
				nullableContentId,
				nullableActionId,
				beanProxifier,
				pointcutPatternVerifiable
		};
	}
	
	public DefaultSettingsParameters() {
		super(parameterDefines);
	}
	
	public DefaultSettingsParameters(String text) {
		super(parameterDefines, text);
	}
	
}
