package com.aspectran.core.var.apon;

import java.util.ArrayList;
import java.util.List;

public class ParameterDefine implements Parameter {

	private final String name;
	
	private final ParameterValueType parameterValueType;
	
	private final boolean array;
	
	private Object value;
	
	private List<Object> list;
	
	private Parameters holder;
	
	public ParameterDefine(String name, ParameterValueType parameterType) {
		this(name, parameterType, false);
	}
	
	public ParameterDefine(String name, ParameterValueType parameterValueType, boolean array) {
		this.name = name;
		this.parameterValueType = parameterValueType;
		
		if(parameterValueType == ParameterValueType.TEXT) {
			this.array = true;
		} else {
			this.array = array;
		}
		
		if(parameterValueType == ParameterValueType.PARAMETERS) {
			this.value = new GenericParameters();
		}
	}

	public ParameterDefine(String name, Parameters parameters) {
		this(name, parameters, false);
	}
	
	public ParameterDefine(String name, Parameters parameters, boolean array) {
		this.name = name;
		this.parameterValueType = ParameterValueType.PARAMETERS;
		this.array = array;
		this.value = parameters;
		
		parameters.setParent(this);
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
			
			if(this.value == null)
				this.value = value;
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
		if(value == null)
			return null;

		return value.toString();
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
	
	public List<String> getValueAsStringList() {
		if(list == null)
			return null;

		if(parameterValueType == ParameterValueType.STRING) {
			return (List<String>)getValueList();
		}
		
		List<String> list2 = new ArrayList<String>();
		
		for(Object o : list) {
			list2.add(o.toString());
		}
		
		return list2;
	}
	
	public String getValueAsText() {
		if(array) {
			if(list == null)
				return null;
			
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < list.size(); i++) {
				sb.append(list.get(i).toString()).append("\n");
			}
			
			return sb.toString();
		} else {
			return getValueAsString();
		}
	}

	public int getValueAsInt() {
		if(parameterValueType != ParameterValueType.INTEGER)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.INTEGER);

		if(value == null)
			return 0;
		
		return ((Integer)value).intValue();
	}
	
	public int[] getValueAsIntArray() {
		List<Integer> intList = getValueAsIntList();
		
		if(intList == null)
			return new int[0];
		
		int[] intArr = new int[intList.size()];
		
		for(int i = 0; i < intArr.length; i++) {
			intArr[i] = ((Integer)intList.get(i)).intValue();
		}
		
		return intArr;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getValueAsIntList() {
		if(parameterValueType != ParameterValueType.INTEGER)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.INTEGER);

		return (List<Integer>)getValueList();
	}
	
	public long getValueAsLong() {
		if(parameterValueType != ParameterValueType.LONG)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.LONG);
		
		if(value == null)
			return 0L;
		
		return ((Long)value).longValue();
	}
	
	public long[] getValueAsLongArray() {
		List<Long> longList = getValueAsLongList();

		if(longList == null)
			return new long[0];
		
		long[] longArr = new long[longList.size()];
		
		for(int i = 0; i < longArr.length; i++) {
			longArr[i] = ((Long)longList.get(i)).longValue();
		}
		
		return longArr;
	}

	@SuppressWarnings("unchecked")
	public List<Long> getValueAsLongList() {
		if(parameterValueType != ParameterValueType.LONG)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.LONG);

		return (List<Long>)getValueList();
	}
	
	public float getValueAsFloat() {
		if(parameterValueType != ParameterValueType.FLOAT)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.FLOAT);

		if(value == null)
			return 0.0F;
		
		return ((Float)value).floatValue();
	}
	
	public float[] getValueAsFloatArray() {
		List<Float> floatList = getValueAsFloatList();

		if(floatList == null)
			return new float[0];
		
		float[] floatArr = new float[floatList.size()];
		
		for(int i = 0; i < floatArr.length; i++) {
			floatArr[i] = ((Float)floatList.get(i)).floatValue();
		}
		
		return floatArr;
	}

	@SuppressWarnings("unchecked")
	public List<Float> getValueAsFloatList() {
		if(parameterValueType != ParameterValueType.FLOAT)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.FLOAT);

		return (List<Float>)getValueList();
	}
	
	public double getValueAsDouble() {
		if(parameterValueType != ParameterValueType.DOUBLE)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.DOUBLE);
		
		if(value == null)
			return 0.0D;
		
		return ((Double)value).doubleValue();
	}
	
	public double[] getValueAsDoubleArray() {
		List<Double> doubleList = getValueAsDoubleList();
		
		if(doubleList == null)
			return new double[0];
				
		double[] doubleArr = new double[doubleList.size()];
		
		for(int i = 0; i < doubleArr.length; i++) {
			doubleArr[i] = ((Double)doubleList.get(i)).doubleValue();
		}
		
		return doubleArr;
	}
	
	@SuppressWarnings("unchecked")
	public List<Double> getValueAsDoubleList() {
		if(parameterValueType != ParameterValueType.DOUBLE)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.DOUBLE);

		return (List<Double>)getValueList();
	}

	public boolean getValueAsBoolean() {
		if(parameterValueType != ParameterValueType.BOOLEAN)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.BOOLEAN);
		
		if(value == null)
			return false;
		
		return ((Boolean)value).booleanValue();
	}

	public boolean[] getValueAsBooleanArray() {
		List<Boolean> booleanList = getValueAsBooleanList();
		
		if(booleanList == null)
			return new boolean[0];
		
		boolean[] booleanArr = new boolean[booleanList.size()];
		
		for(int i = 0; i < booleanArr.length; i++) {
			booleanArr[i] = ((Boolean)booleanList.get(i)).booleanValue();
		}
		
		return booleanArr;
	}
	
	@SuppressWarnings("unchecked")
	public List<Boolean> getValueAsBooleanList() {
		if(parameterValueType != ParameterValueType.BOOLEAN)
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.BOOLEAN);

		return (List<Boolean>)getValueList();
	}
	
	public Parameters getValueAsParameters() {
		if(value == null && parameterValueType == ParameterValueType.VARIABLE) {
			value = new GenericParameters();
		} else if(parameterValueType != ParameterValueType.PARAMETERS) {
			throw new IncompatibleParameterValueTypeException(this, ParameterValueType.PARAMETERS);
		}
		
		return (Parameters)value;
	}

	public Parameters[] getValueAsParametersArray() {
		List<Parameters> parametersList = getValueAsParametersList();
		
		if(parametersList == null)
			return null;
		
		return parametersList.toArray(new Parameters[parametersList.size()]);
	}

	@SuppressWarnings("unchecked")
	public List<Parameters> getValueAsParametersList() {
		if(parameterValueType != ParameterValueType.PARAMETERS)
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
		if(array)
			sb.append(", arraySize=").append(getArraySize());
		sb.append(", qualifiedName=").append(getQualifiedName());
		sb.append("}");
		
		return sb.toString();
	}

}
