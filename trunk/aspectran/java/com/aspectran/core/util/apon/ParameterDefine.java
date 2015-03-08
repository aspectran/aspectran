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
		this.array = (parameterValueType == ParameterValueType.TEXT) ? true : array;
		
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
			/*
			Parameters parameters;
			
			try {
				parameters = parametersClass.newInstance();
			} catch(ReflectiveOperationException e) {
				throw new InvalidParameterException("Could not instantiate parameters class " + this, e);
			}
			*/
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
