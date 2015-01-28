package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterDefine implements Parameter {

	private final String name;
	
	private final ParameterValueType parameterValueType;
	
	private final boolean array;
	
	private int arraySize;

	private Object value;
	
	private Parameters holder;
	
	public ParameterDefine(String name, ParameterValueType parameterType) {
		this(name, parameterType, false);
	}
	
	public ParameterDefine(String name, ParameterValueType parameterValueType, boolean array) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		this.array = array;
	}

	public ParameterDefine(String name, Parameters parameters) {
		this(name, parameters, false);
	}
	
	public ParameterDefine(String name, Parameters parameters, boolean array) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.array = array;
		
		parameters.setParent(this);
		
		this.value = parameters;
	}
	
	protected Parameters getHolder() {
		return holder;
	}

	protected void setHolder(Parameters holder) {
		this.holder = holder;
	}

	public String getName() {
		return name;
	}

	public String getQualifiedName() {
		if(holder == null)
			return name;
		
		ParameterDefine parent = holder.getParent();
		
		if(parent != null)
			return parent.getQualifiedName() + "." + name;
		
		if(holder.getTitle() == null)
			return name;
		
		return holder.getTitle() + "." + name;
	}

	public ParameterValueType getParameterValueType() {
		return parameterValueType;
	}

	public boolean isArray() {
		return array;
	}

	public int getArraySize() {
		return arraySize;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		if(array) {
			addValue(value);
		} else {
			this.value = value;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Parameters addParameters() {
		Class<? extends AbstractParameters> type;
		
		if(arraySize == 0) {
			type = (Class<? extends AbstractParameters>)value.getClass();
		} else {
			type = (Class<? extends AbstractParameters>)((List<Object>)this.value).get(0);
		}
		
		try {
			Parameters p = (Parameters)type.newInstance();
			addValue(p);

			return p;
		} catch(Exception e) {
			throw new InvalidParameterException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized void addValue(Object value) {
		if(this.value == null) {
			this.value = new ArrayList<Object>();
		}
		
		((List<Object>)this.value).add(value);
		arraySize = ((List<Object>)this.value).size();
	}
	
	@SuppressWarnings("unchecked")
	public Object[] getValues() {
		if(this.value == null)
			return null;

		if(array && arraySize > 0) {
			return ((List<Object>)this.value).toArray(new Object[((List<Object>)this.value).size()]);
		} else {
			return new Object[] { this.value };
		}
	}

	@SuppressWarnings("unchecked")
	public List<?> getValueList() {
		if(this.value == null)
			return null;

		if(array && arraySize > 0) {
			return (List<Object>)this.value;
		} else {
			List<Object> list = new ArrayList<Object>();
			list.add(this.value);
			
			return list;
		}
	}

	@SuppressWarnings("unchecked")
	public String getValueAsString() {
		if(value == null)
			return null;

		if(array && arraySize > 0) {
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < ((List<Object>)value).size(); i++) {
				sb.append(((List<Object>)value).get(i).toString()).append("\n");
			}
			
			return sb.toString();
		} else {
			return value.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String[] getValueAsStringArray() {
		if(value == null)
			return null;
		
		if(array && arraySize > 0) {
			String[] s = new String[((List<Object>)value).size()];
			
			for(int i = 0; i < s.length; i++) {
				s[i] = ((List<Object>)value).get(i).toString();
			}
			
			return s;
		} else {
			return new String[] { value.toString() };
		}
	}

	public int getValueAsInt() {
		if(value == null)
			return 0;
		
		if(parameterValueType != ParameterValueType.INTEGER)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.INTEGER);
		
		return ((Integer)value).intValue();
	}

	@SuppressWarnings("unchecked")
	public int[] getValueAsIntArray() {
		if(value == null)
			return new int[0];
		
		if(parameterValueType != ParameterValueType.INTEGER || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.INTEGER);
		
		int[] intArr = new int[((List<Object>)value).size()];
		
		for(int i = 0; i < intArr.length; i++) {
			intArr[i] = ((Integer)((List<Object>)value).get(i)).intValue();
		}
		
		return intArr;
	}

	public long getValueAsLong() {
		if(value == null)
			return 0L;
		
		if(parameterValueType != ParameterValueType.LONG)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.LONG);
		
		return ((Long)value).longValue();
	}
	
	@SuppressWarnings("unchecked")
	public long[] getValueAsLongArray() {
		if(value == null)
			return new long[0];
		
		if(parameterValueType != ParameterValueType.LONG || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.LONG);
		
		long[] longArr = new long[((List<Object>)value).size()];
		
		for(int i = 0; i < longArr.length; i++) {
			longArr[i] = ((Long)((List<Object>)value).get(i)).longValue();
		}
		
		return longArr;
	}

	public float getValueAsFloat() {
		if(value == null)
			return 0.0F;
		
		if(parameterValueType != ParameterValueType.FLOAT)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.FLOAT);
		
		return ((Float)value).floatValue();
	}
	
	@SuppressWarnings("unchecked")
	public float[] getValueAsFloatArray() {
		if(value == null)
			return new float[0];
		
		if(parameterValueType != ParameterValueType.FLOAT || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.FLOAT);
		
		float[] floatArr = new float[((List<Object>)value).size()];
		
		for(int i = 0; i < floatArr.length; i++) {
			floatArr[i] = ((Float)((List<Object>)value).get(i)).floatValue();
		}
		
		return floatArr;
	}

	public double getValueAsDouble() {
		if(value == null)
			return 0.0D;
		
		if(parameterValueType != ParameterValueType.DOUBLE)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.DOUBLE);
		
		return ((Double)value).doubleValue();
	}

	@SuppressWarnings("unchecked")
	public double[] getValueAsDoubleArray() {
		if(value == null)
			return new double[0];
		
		if(parameterValueType != ParameterValueType.DOUBLE || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.DOUBLE);
		
		double[] doubleArr = new double[((List<Object>)value).size()];
		
		for(int i = 0; i < doubleArr.length; i++) {
			doubleArr[i] = ((Double)((List<Object>)value).get(i)).doubleValue();
		}
		
		return doubleArr;
	}
	
	public boolean getValueAsBoolean() {
		if(value == null)
			return false;
		
		if(parameterValueType != ParameterValueType.BOOLEAN)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.BOOLEAN);
		
		return ((Boolean)value).booleanValue();
	}

	@SuppressWarnings("unchecked")
	public boolean[] getValueAsBooleanArray() {
		if(value == null)
			return new boolean[0];
		
		if(parameterValueType != ParameterValueType.BOOLEAN || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.BOOLEAN);
		
		boolean[] booleanArr = new boolean[((List<Object>)value).size()];
		
		for(int i = 0; i < booleanArr.length; i++) {
			booleanArr[i] = ((Boolean)((List<Object>)value).get(i)).booleanValue();
		}
		
		return booleanArr;
	}
	
	public Parameters getParameters() {
		if(value == null && parameterValueType == ParameterValueType.VARIABLE) {
			value = new GenericParameters();
		} else if(parameterValueType != ParameterValueType.PARAMETERS) {
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		}
		
		return (Parameters)value;
	}

	protected Parameters getParameters(int index) {
		List<Parameters> parametersList = getParametersList();
		
		if(parametersList == null)
			return null;
		
		return parametersList.get(index);
	}

	@SuppressWarnings("unchecked")
	public Parameters[] getParametersArray() {
		if(parameterValueType != ParameterValueType.PARAMETERS || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		
		if(arraySize > 0) {
			return ((List<Object>)this.value).toArray(new Parameters[((List<Object>)this.value).size()]);
		} else {
			return new Parameters[] { (Parameters)this.value };
		}
	}

	@SuppressWarnings("unchecked")
	public List<Parameters> getParametersList() {
		if(parameterValueType != ParameterValueType.PARAMETERS || !array)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		
		return (List<Parameters>)getValueList();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{name=").append(name);
		sb.append(", parameterValueType=").append(parameterValueType);
		sb.append(", array=").append(array);
		sb.append(", arraySize=").append(arraySize);
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}

}
