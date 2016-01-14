package com.aspectran.core.context.template;

import java.util.HashMap;

import com.aspectran.core.activity.Activity;

public class TemplateDataMap extends HashMap<String, String> {

	/** @serial */
	private static final long serialVersionUID = 4521506828465323127L;
	
	private final Activity activity;
	
	public TemplateDataMap(Activity activity) {
		this.activity = activity;
	}
	
	
	
}
