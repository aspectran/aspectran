package com.aspectran.web.startup.servlet;

import com.aspectran.core.var.option.AbstractOptions;
import com.aspectran.core.var.option.InvalidOptionException;
import com.aspectran.core.var.option.Option;
import com.aspectran.core.var.option.OptionValueType;
import com.aspectran.core.var.option.Options;

public class SchedulerOptions extends AbstractOptions implements Options {

	public static final Option startDelaySeconds = new Option("startDelaySeconds", OptionValueType.INTEGER);

	public static final Option waitOnShutdown = new Option("waitOnShutdown", OptionValueType.BOOLEAN);
	
	public static final Option autoReload = new Option("autoReload", OptionValueType.STRING);
	
	public static final Option startup = new Option("startup", OptionValueType.BOOLEAN);
	
	private final static Option[] options;
	
	static {
		options = new Option[] {
				startDelaySeconds,
				waitOnShutdown,
				startup
		};
	}
	
	public SchedulerOptions() {
		super(SchedulerOptions.class.getName(), options);
	}
	
	public SchedulerOptions(String plaintext) throws InvalidOptionException {
		super(SchedulerOptions.class.getName(), options, plaintext);
	}

}
