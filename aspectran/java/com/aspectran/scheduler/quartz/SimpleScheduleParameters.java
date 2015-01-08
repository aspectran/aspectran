package com.aspectran.scheduler.quartz;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterDefine;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class SimpleScheduleParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine withIntervalInMilliseconds = new ParameterDefine("withIntervalInMilliseconds", ParameterValueType.INTEGER);

	public static final ParameterDefine withIntervalInMinutes = new ParameterDefine("withIntervalInMinutes", ParameterValueType.INTEGER);
	
	public static final ParameterDefine withIntervalInSeconds = new ParameterDefine("withIntervalInSeconds", ParameterValueType.INTEGER);
	
	public static final ParameterDefine withIntervalInHours = new ParameterDefine("withIntervalInHours", ParameterValueType.INTEGER);
	
	public static final ParameterDefine withRepeatCount = new ParameterDefine("withRepeatCount", ParameterValueType.INTEGER);
	
	public static final ParameterDefine repeatForever = new ParameterDefine("repeatForever", ParameterValueType.BOOLEAN);
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		parameterDefines = new ParameterDefine[] {
			withIntervalInMilliseconds,
			withIntervalInMinutes,
			withIntervalInSeconds,
			withIntervalInHours,
			withRepeatCount,
			repeatForever
		};
	}
	
	public SimpleScheduleParameters() {
		super(SimpleScheduleParameters.class.getName(), parameterDefines);
	}
	
	public SimpleScheduleParameters(String plaintext) throws InvalidParameterException {
		super(SimpleScheduleParameters.class.getName(), parameterDefines, plaintext);
	}
	
}
