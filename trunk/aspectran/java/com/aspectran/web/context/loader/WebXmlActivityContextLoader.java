package com.aspectran.web.context.loader;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.XmlActivityContextLoader;

public class WebXmlActivityContextLoader extends XmlActivityContextLoader {

	private static final String DEFAULT_ROOT_CONTEXT = "/WEB-INF/aspectran/root.xml";
	
	public WebXmlActivityContextLoader() {
		super();
	}
	
	public WebXmlActivityContextLoader(String applicationBasePath) {
		super(applicationBasePath);
	}
	
	public ActivityContext load(String rootContext) {
		if(rootContext == null || rootContext.length() == 0)
			rootContext = DEFAULT_ROOT_CONTEXT;
		
		return super.load(rootContext);
	}
	
}
