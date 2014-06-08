package com.aspectran.web.startup.listener;

import com.aspectran.core.var.option.AbstractOptions;
import com.aspectran.core.var.option.InvalidOptionException;
import com.aspectran.core.var.option.Option;
import com.aspectran.core.var.option.OptionValueType;
import com.aspectran.core.var.option.Options;
import com.aspectran.web.startup.servlet.AutoReloadingOptions;

public class AspectranSchedulerOptions extends AbstractOptions implements Options {

	public static final Option contextConfigLocation = new Option("contextConfigLocation", OptionValueType.STRING);

	public static final Option startDelaySeconds = new Option("startDelaySeconds", OptionValueType.INTEGER);
	
	public static final Option waitOnShutdown = new Option("waitOnShutdown", OptionValueType.BOOLEAN);
	
	public static final Option startup = new Option("startup", OptionValueType.BOOLEAN);
	
	public static final Option autoReloading = new Option("autoReloading", OptionValueType.OPTIONS, new AutoReloadingOptions());
	
	private final static Option[] options;
	
	static {
		options = new Option[] {
				contextConfigLocation,
				startDelaySeconds,
				waitOnShutdown,
				startup,
				autoReloading
		};
	}
	
	public AspectranSchedulerOptions(String patternString) throws InvalidOptionException {
		super(options, AspectranSchedulerOptions.class.getName());
		parse(patternString);
	}
	
}
