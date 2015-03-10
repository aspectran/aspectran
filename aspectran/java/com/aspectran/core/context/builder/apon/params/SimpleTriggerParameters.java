package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class SimpleTriggerParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine withIntervalInMilliseconds;
	public static final ParameterDefine withIntervalInMinutes;
	public static final ParameterDefine withIntervalInSeconds;
	public static final ParameterDefine withIntervalInHours;
	public static final ParameterDefine withRepeatCount;
	public static final ParameterDefine repeatForever;

	private final static ParameterDefine[] parameterDefines;
	
	static {
		withIntervalInMilliseconds = new ParameterDefine("withIntervalInMilliseconds", ParameterValueType.INT);
		withIntervalInMinutes = new ParameterDefine("withIntervalInMinutes", ParameterValueType.INT);
		withIntervalInSeconds = new ParameterDefine("withIntervalInSeconds", ParameterValueType.INT);
		withIntervalInHours = new ParameterDefine("withIntervalInHours", ParameterValueType.INT);
		withRepeatCount = new ParameterDefine("withRepeatCount", ParameterValueType.INT);
		repeatForever = new ParameterDefine("repeatForever", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				withIntervalInMilliseconds,
				withIntervalInMinutes,
				withIntervalInSeconds,
				withIntervalInHours,
				withRepeatCount,
				repeatForever
		};
	}
	
	public SimpleTriggerParameters() {
		super(parameterDefines);
	}
	
	public SimpleTriggerParameters(String text) {
		super(parameterDefines, text);
	}
	
}
