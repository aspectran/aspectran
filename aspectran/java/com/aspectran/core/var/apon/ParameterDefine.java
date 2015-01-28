package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterDefine implements Parameter {

	private final String name;
	
	private final ParameterValueType parameterValueType;
	
	private final boolean array;
	
	private int arraySize;

	private Object value;
	
	private List<Object> list;
	
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
		if(list == null)
			return 0;
		
		return list.size();
	}

	public Object getValue() {
		return value;
	}
	
	public void putValue(Object value) {
		if(array) {
			addValue(value);
		} else {
			this.value = value;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Parameters newParameters() {
		if(parameterValueType != ParameterValueType.PARAMETERS)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);

		if(list == null)
			return (Parameters)value;
		
		Class<? extends AbstractParameters> type = (Class<? extends AbstractParameters>)value.getClass();
		
		try {
			Parameters p = (Parameters)type.newInstance();
			p.setParent(this);
			return p;
		} catch(Exception e) {
			throw new InvalidParameterException(e);
		}
	}
	
	private synchronized void addValue(Object value) {
		if(list == null) {
			list = new ArrayList<Object>();
		}
		
		list.add(value);
	}
	
	public Object[] getValues() {
		if(list == null)
			return null;

		return list.toArray(new Object[list.size()]);
	}

	public List<?> getValueList() {
		return list;
	}

	public String getValueAsString() {
		if(array) {
			if(list == null)
				return null;
			
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < list.size(); i++) {
				sb.append(list.get(i).toString()).append("\n");
			}
			
			return sb.toString();
		} else {
			if(value == null)
				return null;

			return value.toString();
		}
	}
	
	public String[] getValueAsStringArray() {
		if(array) {
			if(list == null)
				return null;

			String[] s = new String[list.size()];
			
			for(int i = 0; i < s.length; i++) {
				s[i] = list.get(i).toString();
			}
			
			return s;
		} else {
			if(value == null)
				return null;

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
