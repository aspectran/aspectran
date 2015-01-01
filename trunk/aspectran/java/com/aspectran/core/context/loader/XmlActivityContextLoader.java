package com.aspectran.core.context.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.XmlActivityContextBuilder;

public class XmlActivityContextLoader implements ActivityContextLoader {

	private final Logger logger = LoggerFactory.getLogger(XmlActivityContextLoader.class);

	private ApplicationAdapter applicationAdapter;
	
	private AspectranClassLoader aspectranClassLoader;
	
	private ActivityContext activityContext;

	public XmlActivityContextLoader() {
	}
	
	public XmlActivityContextLoader(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
	
	public ApplicationAdapter getApplicationAdapter() {
		return applicationAdapter;
	}

	public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
		this.applicationAdapter = applicationAdapter;
	}
	
	public AspectranClassLoader getAspectranClassLoader() {
		return aspectranClassLoader;
	}

	public void setAspectranClassLoader(AspectranClassLoader aspectranClassLoader) {
		this.aspectranClassLoader = aspectranClassLoader;
	}

	public ActivityContext load(String rootContext) {
		return buildXmlActivityContext(rootContext);
	}

	protected ActivityContext buildXmlActivityContext(String rootContext) {
		logger.info("build ActivityContext [" + rootContext + "]");
		long startTime = System.currentTimeMillis();

		ActivityContextBuilder builder = new XmlActivityContextBuilder(applicationAdapter, aspectranClassLoader);
		activityContext = builder.build(rootContext);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("ActivityContext build completed in " + elapsedTime + " ms");
		
		return activityContext;
	}
	
}
