package com.aspectran.web.startup.loader;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.ActivityContextLoader;

public class WebActivityContextLoader extends ActivityContextLoader {

	public static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";
	
	private static final String DEFAULT_ROOT_CONTEXT = "/WEB-INF/aspectran/aspectranContext.xml";
	
	public WebActivityContextLoader(ApplicationAdapter applicationAdapter) {
		super(applicationAdapter);
	}
	
	public ActivityContext load(String rootContext) {
		if(rootContext == null || rootContext.length() == 0)
			rootContext = DEFAULT_ROOT_CONTEXT;
		
		return super.load(rootContext);
	}
	
}
