package com.aspectran.web.startup.servlet;

import com.aspectran.core.var.option.AbstractOptions;
import com.aspectran.core.var.option.InvalidOptionException;
import com.aspectran.core.var.option.Option;
import com.aspectran.core.var.option.OptionValueType;
import com.aspectran.core.var.option.Options;

/**
 * The Class AutoReloadingOptions.
 * 
 * 			observingPath: [
					classpath:**
					WEB-INF/config/sqlmap/**
				]
				observationInterval: 5
				startup: false
 */
public class AutoReloadingOptions extends AbstractOptions implements Options {

	public static final Option observingPath = new Option("observingPath", OptionValueType.STRING_ARRAY);

	public static final Option observationInterval = new Option("observationInterval", OptionValueType.INTEGER);
	
	public static final Option startup = new Option("startup", OptionValueType.BOOLEAN);
	
	private final static Option[] options;
	
	static {
		options = new Option[] {
				observingPath,
				observationInterval,
				startup
		};
	}
	
	public AutoReloadingOptions() {
		super(AutoReloadingOptions.class.getName(), options);
	}
	
	public AutoReloadingOptions(String plaintext) throws InvalidOptionException {
		super(AutoReloadingOptions.class.getName(), options, plaintext);
	}
	
}
