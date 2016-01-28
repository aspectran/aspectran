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
package com.aspectran.core.util.apon;


public class GenericParameters extends AbstractParameters implements Parameters {

	public GenericParameters() {
		super(null);
	}
	
	public GenericParameters(String text) {
		super(null, text);
	}

	public GenericParameters(ParameterDefine[] parameterDefines) {
		super(parameterDefines);
	}
	
	public GenericParameters(ParameterDefine[] parameterDefines, String text) {
		super(parameterDefines, text);
	}
	
	public void putValue(String name, Object value) {
		Parameter p = touchParameterValue(name, value);
		p.putValue(value);
	}
	
	private Parameter touchParameterValue(String name, Object value) {
		Parameter p = parameterValueMap.get(name);
		
		if(p == null && isAddable())
			p = newParameterValue(name, determineParameterValueType(value));
		
		if(p == null)
			throw new UnknownParameterException(name, this);
		
		return p;
	}

	private ParameterValueType determineParameterValueType(Object value) {
		ParameterValueType parameterValueType;
		
		if(value instanceof String) {
			if(value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) == -1)
				parameterValueType = ParameterValueType.STRING;
			else
				parameterValueType = ParameterValueType.TEXT;
		} else if(value instanceof Integer) {
			parameterValueType = ParameterValueType.INT;
		} else if(value instanceof Long) {
			parameterValueType = ParameterValueType.LONG;
		} else if(value instanceof Float) {
			parameterValueType = ParameterValueType.FLOAT;
		} else if(value instanceof Double) {
			parameterValueType = ParameterValueType.DOUBLE;
		} else if(value instanceof Boolean) {
			parameterValueType = ParameterValueType.BOOLEAN;
		} else if(value instanceof Parameters) {
			parameterValueType = ParameterValueType.PARAMETERS;
		} else {
			parameterValueType = ParameterValueType.STRING;
		}
		
		return parameterValueType;
	}

}
