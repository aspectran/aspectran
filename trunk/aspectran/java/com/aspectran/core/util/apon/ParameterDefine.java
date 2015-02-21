package com.aspectran.core.util.apon;

public class ParameterDefine {

	private final String name;
	
	private final ParameterValueType parameterValueType;
	
	private final Class<? extends AbstractParameters> parametersClass;
	
	private final boolean array;
	
	public ParameterDefine(String name, ParameterValueType parameterType) {
		this(name, parameterType, false);
	}
	
	public ParameterDefine(String name, ParameterValueType parameterValueType, boolean array) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.parametersClass = null;
		
		if(parameterValueType == ParameterValueType.TEXT) {
			this.array = true;
		} else {
			this.array = array;
		}
	}

	public ParameterDefine(String name, Class<? extends AbstractParameters> parametersClass) {
		this(name, parametersClass, false);
	}
	
	public ParameterDefine(String name, Class<? extends AbstractParameters> parametersClass, boolean array) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.parametersClass = parametersClass;
		this.array = array;
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
	
	public ParameterValue newParameterValue() {
		ParameterValue parameterValue;
		
		if(parameterValueType == ParameterValueType.PARAMETERS && parametersClass != null) {
			Parameters parameters;
			
			try {
				parameters = parametersClass.newInstance();
			} catch(ReflectiveOperationException e) {
				throw new InvalidParameterException("Could not instantiate parameters class " + this, e);
			}
			
			parameterValue = new ParameterValue(name, parameters, array);
		} else {
			parameterValue = new ParameterValue(name, parameterValueType, array);
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
