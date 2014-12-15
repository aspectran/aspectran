package com.aspectran.scheduler.quartz;

import com.aspectran.core.var.apon.AbstractParameters;
import com.aspectran.core.var.apon.InvalidParameterException;
import com.aspectran.core.var.apon.ParameterValue;
import com.aspectran.core.var.apon.ParameterValueType;
import com.aspectran.core.var.apon.Parameters;

public class SimpleScheduleParameters extends AbstractParameters implements Parameters {

	public static final ParameterValue withIntervalInMilliseconds = new ParameterValue("withIntervalInMilliseconds", ParameterValueType.INTEGER);

	public static final ParameterValue withIntervalInMinutes = new ParameterValue("withIntervalInMinutes", ParameterValueType.INTEGER);
	
	public static final ParameterValue withIntervalInSeconds = new ParameterValue("withIntervalInSeconds", ParameterValueType.INTEGER);
	
	public static final ParameterValue withIntervalInHours = new ParameterValue("withIntervalInHours", ParameterValueType.INTEGER);
	
	public static final ParameterValue withRepeatCount = new ParameterValue("withRepeatCount", ParameterValueType.INTEGER);
	
	public static final ParameterValue repeatForever = new ParameterValue("repeatForever", ParameterValueType.BOOLEAN);
	
	private final static ParameterValue[] parameterValues;
	
	static {
		parameterValues = new ParameterValue[] {
				withIntervalInMilliseconds,
				withIntervalInMinutes,
				withIntervalInSeconds,
				withIntervalInHours,
				withRepeatCount,
				repeatForever
		};
	}
	
	public SimpleScheduleParameters() {
		super(SimpleScheduleParameters.class.getName(), parameterValues);
	}
	
	public SimpleScheduleParameters(String plaintext) throws InvalidParameterException {
		super(SimpleScheduleParameters.class.getName(), parameterValues, plaintext);
	}
	
}
