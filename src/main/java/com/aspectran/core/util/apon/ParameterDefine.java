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
package com.aspectran.core.util.apon;

public class ParameterDefine {

	private final String name;
	
	private final ParameterValueType parameterValueType;
	
	private final Class<? extends AbstractParameters> parametersClass;
	
	private final boolean array;
	
	private final boolean noBracket;
	
	public ParameterDefine(String name, ParameterValueType parameterValueType) {
		this(name, parameterValueType, false);
	}
	
	public ParameterDefine(String name, ParameterValueType parameterValueType, boolean array) {
		this(name, parameterValueType, array, false);
	}
	
	public ParameterDefine(String name, ParameterValueType parameterValueType, boolean array, boolean noBracket) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.parametersClass = null;
		this.array = array;
		
		if(this.array && parameterValueType == ParameterValueType.PARAMETERS)
			this.noBracket = noBracket;
		else
			this.noBracket = false;
	}

	public ParameterDefine(String name, Class<? extends AbstractParameters> parametersClass) {
		this(name, parametersClass, false);
	}
	
	public ParameterDefine(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
		this(name, parametersClass, array, false);
	}
	
	public ParameterDefine(String name, Class<? extends AbstractParameters> parametersClass, boolean array, boolean noBracket) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.parametersClass = parametersClass;
		this.array = array;
		
		if(this.array && parameterValueType == ParameterValueType.PARAMETERS)
			this.noBracket = noBracket;
		else
			this.noBracket = false;
	}
	
	public String getName() {
		return name;
	}

	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	public boolean isArray() {
		return array;
	}
	
	public boolean isNoBracket() {
		return noBracket;
	}

	public ParameterValue newParameterValue() {
		ParameterValue parameterValue;
		
		if(parameterValueType == ParameterValueType.PARAMETERS && parametersClass != null) {
			parameterValue = new ParameterValue(name, parametersClass, array, noBracket, true);
		} else {
			parameterValue = new ParameterValue(name, parameterValueType, array, noBracket, true);
		}

		return parameterValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", parameterValueType=").append(parameterValueType);
		sb.append(", parameterClass=").append(parametersClass);
		sb.append(", array=").append(array);
		sb.append("}");
		
		return sb.toString();
	}

}
