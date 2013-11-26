package com.aspectran.scheduler.quartz;

import com.aspectran.core.var.option.AbstractOptions;
import com.aspectran.core.var.option.InvalidOptionException;
import com.aspectran.core.var.option.Option;
import com.aspectran.core.var.option.OptionValueType;
import com.aspectran.core.var.option.Options;

public class SimpleScheduleOptions extends AbstractOptions implements Options {

	public static final Option withIntervalInMilliseconds = new Option("withIntervalInMilliseconds", OptionValueType.INTEGER);

	public static final Option withIntervalInMinutes = new Option("withIntervalInMinutes", OptionValueType.INTEGER);
	
	public static final Option withIntervalInSeconds = new Option("withIntervalInSeconds", OptionValueType.INTEGER);
	
	public static final Option withIntervalInHours = new Option("withIntervalInHours", OptionValueType.INTEGER);
	
	public static final Option withRepeatCount = new Option("withRepeatCount", OptionValueType.INTEGER);
	
	public static final Option repeatForever = new Option("repeatForever", OptionValueType.BOOLEAN);
	
	private final static Option[] options;
	
	static {
		options = new Option[] {
				withIntervalInMilliseconds,
				withIntervalInMinutes,
				withIntervalInSeconds,
				withIntervalInHours,
				withRepeatCount,
				repeatForever
		};
	}
	
	public SimpleScheduleOptions(String patternString) throws InvalidOptionException {
		super(options, SimpleScheduleOptions.class.getName());
		parse(patternString);
	}
	
}
